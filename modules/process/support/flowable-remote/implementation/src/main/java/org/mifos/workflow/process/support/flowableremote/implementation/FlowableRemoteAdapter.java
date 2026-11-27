///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.flowableremote.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN;
import static org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportConstants.FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_ENABLED;
import static org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportConstants.FLOWABLE_REMOTE_WORKFLOW_SUPPORT_VARIABLE_TYPE;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.mifos.workflow.process.core.dispatch.MifosProcessDispatcher;
import org.mifos.workflow.process.support.flowableremote.core.FlowableRemoteSupportProperties;
import org.mifos.workflow.process.support.flowableremote.implementation.FlowableRemoteJobs.Job;
import org.mifos.workflow.process.support.flowableremote.implementation.FlowableRemoteJobs.Variable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/// Lets service tasks of a remote Flowable engine run Mifos flows and usecases.
///
/// A remote engine cannot call beans from expressions, so service tasks are external worker tasks with the
/// `mifosAdapter` topic. An execution listener sets the flow input or usecase request type as the local variable
/// `type` of the task; the job carries the variables of the task's execution, including the input variable:
///
/// ```xml
/// <serviceTask id="createLoan" flowable:type="external-worker" flowable:topic="mifosAdapter">
///   <extensionElements>
///     <flowable:executionListener event="start"
///         expression="${execution.setVariableLocal('type', 'com.acme.LoanCreateUsecase.Request')}"/>
///   </extensionElements>
/// </serviceTask>
/// ```
///
/// While the application context runs, a virtual thread acquires jobs; when none is available it acquires again
/// after `poll-interval`. The output is written to the output variable (default `output`), as JSON text unless
/// `serialize-result` is disabled.
///
/// A missing input, type or unknown type cannot succeed on retry, so the job fails without retries and becomes a
/// dead letter job right away; any other error uses up one retry.
@Slf4j
@Component(MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN)
@ConditionalOnBooleanProperty(FLOWABLE_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_ENABLED)
public final class FlowableRemoteAdapter implements SmartLifecycle {
    private static final Duration STOP_TIMEOUT = Duration.ofSeconds(10);

    private final MifosProcessDispatcher dispatcher;
    private final FlowableRemoteSupportProperties properties;
    private final FlowableRemoteJobs jobs;

    private volatile @Nullable ScheduledExecutorService worker;

    FlowableRemoteAdapter(
            MifosProcessDispatcher dispatcher, FlowableRemoteSupportProperties properties, RestClient.Builder builder) {
        this.dispatcher = dispatcher;
        this.properties = properties;
        this.jobs = new FlowableRemoteJobs(builder, properties);
    }

    @Override
    public synchronized void start() {
        if (worker == null) {
            var executor = Executors.newSingleThreadScheduledExecutor(
                    Thread.ofVirtual().name("mifos-flowable-worker").factory());
            var interval = properties.pollInterval().toMillis();
            executor.scheduleWithFixedDelay(this::poll, 0, interval, TimeUnit.MILLISECONDS);
            worker = executor;

            log.info("acquiring external worker jobs of topic {} from {}", properties.topic(), properties.baseUrl());
        }
    }

    @Override
    public synchronized void stop() {
        var executor = worker;
        worker = null;

        if (executor != null) {
            executor.shutdownNow();

            try {
                if (!executor.awaitTermination(STOP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                    log.warn("external worker did not stop within {}", STOP_TIMEOUT);
                }
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return worker != null;
    }

    /// Acquires and runs jobs until none is available; the executor runs it again after `poll-interval`.
    ///
    /// Never throws, because an exception would cancel all later runs of a scheduled task.
    private void poll() {
        while (worker != null && !Thread.currentThread().isInterrupted()) {
            List<Job> acquired;

            try {
                acquired = jobs.acquire();
            } catch (RuntimeException e) {
                if (worker != null) {
                    log.warn("acquiring external worker jobs from {} failed", properties.baseUrl(), e);
                }

                return;
            }

            if (acquired.isEmpty()) {
                return;
            }

            acquired.forEach(this::execute);
        }
    }

    /// Runs the flow or usecase named by the job's `type` variable with the job's input variable and completes the
    /// job with its output.
    void execute(Job job) {
        var variables = new HashMap<String, @Nullable Object>();
        Objects.requireNonNullElse(job.variables(), List.<Variable>of())
                .forEach(variable -> variables.put(variable.name(), variable.value()));
        var type = variables.get(FLOWABLE_REMOTE_WORKFLOW_SUPPORT_VARIABLE_TYPE);

        Object output;
        try {
            output = run(type == null ? null : type.toString(), variables);
        } catch (IllegalArgumentException e) {
            fail(job, 0, e);
            return;
        } catch (RuntimeException e) {
            fail(job, Objects.requireNonNullElse(job.retries(), 1) - 1, e);
            return;
        }

        try {
            jobs.complete(job, List.of(toVariable(properties.outputVariable(), output)));

            log.debug("ran {} in activity {}", type, job.elementId());
        } catch (RuntimeException e) {
            // the lock expires and the engine gives the job again
            log.warn("completing job {} in activity {} failed", job.id(), job.elementId(), e);
        }
    }

    private @Nullable Object run(@Nullable String type, Map<String, @Nullable Object> variables) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                    "Job variable '" + FLOWABLE_REMOTE_WORKFLOW_SUPPORT_VARIABLE_TYPE + "' is required");
        }
        var input = variables.get(properties.inputVariable());
        if (input == null && properties.failOnNullInput()) {
            throw new IllegalArgumentException(
                    "Variable '" + properties.inputVariable() + "' is required to run " + type);
        }
        var response = dispatcher.dispatch(type, input);
        return response != null && properties.serializeResult() ? dispatcher.toJson(response) : response;
    }

    private static Variable toVariable(String name, @Nullable Object value) {
        return switch (value) {
            case null -> new Variable(name, null, null);
            case String s -> new Variable(name, "string", s);
            default -> new Variable(name, "json", value);
        };
    }

    private void fail(Job job, int retries, RuntimeException e) {
        log.warn("job {} in activity {} failed", job.id(), job.elementId(), e);
        try {
            jobs.fail(
                    job, String.valueOf(e.getMessage()), e.toString(), Math.max(retries, 0), properties.retryTimeout());
        } catch (RuntimeException failure) {
            log.warn("reporting the failure of job {} failed", job.id(), failure);
        }
    }
}
