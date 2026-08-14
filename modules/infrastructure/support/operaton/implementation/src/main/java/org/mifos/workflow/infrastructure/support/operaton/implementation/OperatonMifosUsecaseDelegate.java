/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.support.operaton.implementation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mifos.boot.commons.service.MifosUsecaseJsonHelper;
import org.mifos.boot.commons.service.MifosUsecaseRegistry;
import org.mifos.commons.boot.core.model.MifosRequest;
import org.mifos.commons.boot.core.model.MifosResponse;
import org.mifos.workflow.infrastructure.support.operaton.core.OperatonFlowSupportProperties;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public final class OperatonMifosUsecaseDelegate implements JavaDelegate {
    private final MifosUsecaseRegistry registry;
    private final MifosUsecaseJsonHelper jsonHelper;
    private final OperatonFlowSupportProperties properties;

    @Getter
    @Setter
    private String type;

    @Override
    public void execute(DelegateExecution execution) {
        // determine request type
        var clazz = resolve(execution);

        // parse input to request POJO
        var request = jsonHelper.parseRequest(execution.getVariable(properties.getInputVariable()), clazz);

        // find and execute usecase
        var response = registry.execute(request);

        // store result
        store(execution, response);
    }

    private Class<? extends MifosRequest> resolve(DelegateExecution execution) {
        // allow override via execution variable
        if (StringUtils.isEmpty(type)) {
            type = (String) execution.getVariable(properties.getTypeOverrideVariable());
        }

        return jsonHelper.resolveRequest(type);
    }

    private void store(DelegateExecution execution, MifosResponse response) {
        if (response == null) {
            execution.setVariable(properties.getOutputVariable(), null);
            return;
        }

        if (properties.isSerializeResult()) {
            execution.setVariable(properties.getOutputVariable(), jsonHelper.serializeResponse(response));
        } else {
            execution.setVariable(properties.getOutputVariable(), response);
        }
    }
}
