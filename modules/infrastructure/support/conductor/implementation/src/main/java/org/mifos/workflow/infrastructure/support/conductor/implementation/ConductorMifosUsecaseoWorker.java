/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.support.conductor.implementation;

import com.google.errorprone.annotations.Var;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mifos.boot.commons.service.MifosUsecaseJsonHelper;
import org.mifos.boot.commons.service.MifosUsecaseRegistry;
import org.mifos.commons.boot.core.model.MifosRequest;
import org.mifos.commons.boot.core.model.MifosResponse;
import org.mifos.workflow.infrastructure.support.conductor.core.ConductorFlowSupportProperties;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public final class ConductorMifosUsecaseoWorker {
    private final MifosUsecaseRegistry registry;
    private final MifosUsecaseJsonHelper jsonHelper;
    private final ConductorFlowSupportProperties properties;

    public TaskResult execute(Task task) {
        var result = new TaskResult(task);

        // determine request type
        var clazz = resolve(task);

        // parse input to request POJO
        var request = jsonHelper.parseRequest(task.getInputData().get(properties.getInputVariable()), clazz);

        // find and execute usecase
        var response = registry.execute(request);

        // store result
        result.setStatus(TaskResult.Status.COMPLETED);
        result.setOutputData(store(response));

        return result;
    }

    private Class<? extends MifosRequest> resolve(Task task) {
        // allow override via execution variable
        @Var var type = (String) task.getInputData().get(properties.getTypeVariable());

        if (StringUtils.isEmpty(type)) {
            type = (String) task.getInputData().get(properties.getTypeOverrideVariable());
        }

        return jsonHelper.resolveRequest(type);
    }

    private Map<String, Object> store(MifosResponse response) {
        if (response == null) {
            return Map.of(properties.getOutputVariable(), null);
        }

        if (properties.isSerializeResult()) {
            return Map.of(properties.getOutputVariable(), jsonHelper.serializeResponse(response));
        } else {
            return Map.of(properties.getOutputVariable(), response);
        }
    }
}
