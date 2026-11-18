///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.support.cibseven.implementation;

import static org.mifos.workflow.process.core.MifosProcessConstants.MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN;
import static org.mifos.workflow.process.support.cibseven.core.CibsevenSupportConstants.CIBSEVEN_WORKFLOW_SUPPORT_PROPERTIES_ENABLED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cibseven.bpm.engine.delegate.DelegateExecution;
import org.mifos.workflow.process.core.dispatch.MifosProcessDispatcher;
import org.mifos.workflow.process.support.cibseven.core.CibsevenSupportProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

/// Lets CIB seven service tasks run Mifos flows and usecases.
///
/// ```xml
/// <serviceTask id="createLoan"
///     camunda:expression="${mifosAdapter.execute(execution, 'com.acme.LoanCreateUsecase.Request')}"/>
/// ```
///
/// The input is read from the input variable (default `input`) and the output written to the output
/// variable (default `output`), as JSON text unless `serialize-result` is disabled.
///
/// The class is public because the engine's expression language only invokes public methods of public classes.
@Slf4j
@RequiredArgsConstructor
@Component(MIFOS_WORKFLOW_PROCESS_ADAPTER_BEAN)
@ConditionalOnBooleanProperty(CIBSEVEN_WORKFLOW_SUPPORT_PROPERTIES_ENABLED)
public final class CibsevenAdapter {
    private final MifosProcessDispatcher dispatcher;
    private final CibsevenSupportProperties properties;

    /// Runs the flow or usecase for `type` with the current execution's input variable and stores its output.
    ///
    /// @param execution the execution of the service task
    /// @param type class name of the flow input or usecase request
    public void execute(DelegateExecution execution, String type) {
        var input = execution.getVariable(properties.inputVariable());

        if (input == null && properties.failOnNullInput()) {
            throw new IllegalArgumentException(
                    "Variable '" + properties.inputVariable() + "' is required to run " + type);
        }

        var response = dispatcher.dispatch(type, input);

        execution.setVariable(
                properties.outputVariable(),
                response != null && properties.serializeResult() ? dispatcher.toJson(response) : response);

        log.debug("ran {} in activity {}", type, execution.getCurrentActivityId());
    }
}
