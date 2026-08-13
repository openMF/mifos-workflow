/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.support.camunda.implementation;

import com.google.errorprone.annotations.Var;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mifos.boot.commons.service.MifosUsecaseJsonHelper;
import org.mifos.boot.commons.service.MifosUsecaseRegistry;
import org.mifos.commons.boot.core.model.MifosRequest;
import org.mifos.commons.boot.core.model.MifosResponse;
import org.mifos.workflow.infrastructure.support.camunda.core.CamundaFlowSupportProperties;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public final class CamundaMifosUsecaseoWorker {
    private final MifosUsecaseRegistry registry;
    private final MifosUsecaseJsonHelper jsonHelper;
    private final CamundaFlowSupportProperties properties;

    @JobWorker(type = "mifos-usecase")
    public Map<String, Object> execute(JobClient client, ActivatedJob job) {
        // determine request type
        var clazz = resolve(job);

        // parse input to request POJO
        var request = jsonHelper.parseRequest(job.getVariable(properties.getInputVariable()), clazz);

        // find and execute usecase
        var response = registry.execute(request);

        // store result
        return store(response);
    }

    private Class<? extends MifosRequest> resolve(ActivatedJob job) {
        // allow override via execution variable
        @Var var type = (String) job.getVariable(properties.getTypeVariable());

        if (StringUtils.isEmpty(type)) {
            type = (String) job.getVariable(properties.getTypeOverrideVariable());
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
