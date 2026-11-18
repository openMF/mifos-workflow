///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.camunda.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN;
import static org.mifos.workflow.process.support.camunda.core.CamundaSupportConstants.CAMUNDA_WORKFLOW_SUPPORT_PROPERTIES_ENABLED;
import static org.mifos.workflow.process.support.camunda.core.CamundaSupportConstants.CAMUNDA_WORKFLOW_SUPPORT_TYPE_HEADER;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.mifos.workflow.process.core.dispatch.MifosProcessDispatcher;
import org.mifos.workflow.process.support.camunda.core.CamundaSupportProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

/// Lets Camunda 8 service tasks run Mifos flows and usecases.
///
/// Camunda 8 cannot call beans from expressions, so service tasks use the `mifosAdapter` job type and name the
/// flow input or usecase request in the `type` task header:
///
/// ```xml
/// <serviceTask id="createLoan">
///   <extensionElements>
///     <zeebe:taskDefinition type="mifosAdapter"/>
///     <zeebe:taskHeaders>
///       <zeebe:header key="type" value="com.acme.LoanCreateUsecase.Request"/>
///     </zeebe:taskHeaders>
///   </extensionElements>
/// </serviceTask>
/// ```
///
/// The input is read from the input variable (default `input`) and the output written to the output
/// variable (default `output`), as JSON text unless `serialize-result` is disabled.
///
/// A missing input, header or unknown type cannot succeed on retry, so the job fails without retries and raises an
/// incident right away; any other error uses up one of the job's retries.
@Slf4j
@RequiredArgsConstructor
@Component(MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN)
@ConditionalOnBooleanProperty(CAMUNDA_WORKFLOW_SUPPORT_PROPERTIES_ENABLED)
public final class CamundaAdapter {
    private final MifosProcessDispatcher dispatcher;
    private final CamundaSupportProperties properties;

    /// Runs the flow or usecase named by the job's `type` header with the job's input variable and completes the
    /// job with its output.
    ///
    /// @param client the client to complete or fail the job with
    /// @param job the activated service task job
    @JobWorker(type = MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN, autoComplete = false)
    public void execute(JobClient client, ActivatedJob job) {
        var type = job.getCustomHeaders().get(CAMUNDA_WORKFLOW_SUPPORT_TYPE_HEADER);

        try {
            var output = run(type, job.getVariablesAsMap().get(properties.inputVariable()));

            // HashMap, because a usecase may legitimately return null
            var variables = new HashMap<String, Object>();
            variables.put(properties.outputVariable(), output);

            client.newCompleteCommand(job.getKey()).variables(variables).send().join();

            log.debug("ran {} in element {}", type, job.getElementId());
        } catch (IllegalArgumentException e) {
            fail(client, job, 0, e);
        } catch (RuntimeException e) {
            fail(client, job, job.getRetries() - 1, e);
        }
    }

    private @Nullable Object run(@Nullable String type, @Nullable Object input) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                    "Task header '" + CAMUNDA_WORKFLOW_SUPPORT_TYPE_HEADER + "' is required");
        }

        if (input == null && properties.failOnNullInput()) {
            throw new IllegalArgumentException(
                    "Variable '" + properties.inputVariable() + "' is required to run " + type);
        }

        var response = dispatcher.dispatch(type, input);

        return response != null && properties.serializeResult() ? dispatcher.toJson(response) : response;
    }

    private static void fail(JobClient client, ActivatedJob job, int retries, RuntimeException e) {
        log.warn("job {} in element {} failed", job.getKey(), job.getElementId(), e);

        client.newFailCommand(job.getKey())
                .retries(Math.max(retries, 0))
                .errorMessage(String.valueOf(e.getMessage()))
                .send()
                .join();
    }
}
