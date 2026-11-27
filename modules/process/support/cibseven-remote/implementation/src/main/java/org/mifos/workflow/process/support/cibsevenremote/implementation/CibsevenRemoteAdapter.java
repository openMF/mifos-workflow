///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.cibsevenremote.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN;
import static org.mifos.workflow.process.support.cibsevenremote.core.CibsevenRemoteSupportConstants.CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_EXTENSION_PROPERTY_TYPE;
import static org.mifos.workflow.process.support.cibsevenremote.core.CibsevenRemoteSupportConstants.CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_ENABLED;

import java.util.HashMap;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.client.ExternalTaskClient;
import org.cibseven.bpm.client.interceptor.auth.BasicAuthProvider;
import org.cibseven.bpm.client.task.ExternalTask;
import org.cibseven.bpm.client.task.ExternalTaskHandler;
import org.cibseven.bpm.client.task.ExternalTaskService;
import org.jspecify.annotations.Nullable;
import org.mifos.workflow.process.core.dispatch.MifosProcessDispatcher;
import org.mifos.workflow.process.support.cibsevenremote.core.CibsevenRemoteSupportProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/// Lets service tasks of a remote CIB seven engine run Mifos flows and usecases.
///
/// A remote engine cannot call beans from expressions, so service tasks are external tasks with the
/// `mifosAdapter` topic and name the flow input or usecase request in the `type` extension property:
///
/// ```xml
/// <serviceTask id="createLoan" camunda:type="external" camunda:topic="mifosAdapter">
///   <extensionElements>
///     <camunda:properties>
///       <camunda:property name="type" value="com.acme.LoanCreateUsecase.Request"/>
///     </camunda:properties>
///   </extensionElements>
/// </serviceTask>
/// ```
///
/// The adapter fetches the tasks with long polling while the application context runs. The input is read from the
/// input variable (default `input`) and the output written to the output variable (default `output`), as JSON
/// text unless `serialize-result` is disabled.
///
/// A missing input, type or unknown type cannot succeed on retry, so the task fails without retries and raises an
/// incident right away; any other error uses up one retry.
@Slf4j
@RequiredArgsConstructor
@Component(MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN)
@ConditionalOnBooleanProperty(CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_PROPERTIES_ENABLED)
public final class CibsevenRemoteAdapter implements ExternalTaskHandler, SmartLifecycle {
    private final MifosProcessDispatcher dispatcher;
    private final CibsevenRemoteSupportProperties properties;

    private volatile @Nullable ExternalTaskClient client;

    @Override
    public void start() {
        var builder = ExternalTaskClient.create()
                .baseUrl(properties.baseUrl())
                .workerId(properties.workerId())
                .maxTasks(properties.maxTasks())
                .asyncResponseTimeout(properties.asyncResponseTimeout().toMillis());
        var username = properties.username();
        if (username != null) {
            builder.addInterceptor(
                    new BasicAuthProvider(username, Objects.requireNonNullElse(properties.password(), "")));
        }
        var started = builder.build();
        started.subscribe(properties.topic())
                .lockDuration(properties.lockDuration().toMillis())
                .variables(properties.inputVariable())
                .includeExtensionProperties(true)
                .handler(this)
                .open();
        client = started;

        log.info("fetching external tasks of topic {} from {}", properties.topic(), properties.baseUrl());
    }

    @Override
    public void stop() {
        var running = client;
        if (running != null) {
            running.stop();
            client = null;
        }
    }

    @Override
    public boolean isRunning() {
        return client != null;
    }

    /// Runs the flow or usecase named by the task's `type` extension property with the task's input variable and
    /// completes the task with its output.
    ///
    /// @param task the fetched external task
    /// @param service the service to complete or fail the task with
    @Override
    public void execute(ExternalTask task, ExternalTaskService service) {
        var type = task.getExtensionProperty(CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_EXTENSION_PROPERTY_TYPE);
        try {
            var output = run(type, task.getVariable(properties.inputVariable()));
            // HashMap, because a usecase may legitimately return null
            var variables = new HashMap<String, Object>();
            variables.put(properties.outputVariable(), output);
            service.complete(task, variables);

            log.debug("ran {} in activity {}", type, task.getActivityId());
        } catch (IllegalArgumentException e) {
            fail(task, service, 0, e);
        } catch (RuntimeException e) {
            // the engine reports no retries until the task failed once
            Integer current = task.getRetries();
            fail(task, service, (current == null ? properties.retries() : current) - 1, e);
        }
    }

    private @Nullable Object run(@Nullable String type, @Nullable Object input) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Extension property '"
                    + CIBSEVEN_REMOTE_WORKFLOW_SUPPORT_EXTENSION_PROPERTY_TYPE + "' is required");
        }
        if (input == null && properties.failOnNullInput()) {
            throw new IllegalArgumentException(
                    "Variable '" + properties.inputVariable() + "' is required to run " + type);
        }
        var response = dispatcher.dispatch(type, input);
        return response != null && properties.serializeResult() ? dispatcher.toJson(response) : response;
    }

    private void fail(ExternalTask task, ExternalTaskService service, int retries, RuntimeException e) {
        log.warn("external task {} in activity {} failed", task.getId(), task.getActivityId(), e);
        service.handleFailure(
                task,
                String.valueOf(e.getMessage()),
                e.toString(),
                Math.max(retries, 0),
                properties.retryTimeout().toMillis());
    }
}
