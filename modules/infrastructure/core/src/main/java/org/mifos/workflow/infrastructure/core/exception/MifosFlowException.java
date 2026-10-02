/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.core.exception;

import static org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_INCREMENT;
import static org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_START;
import static org.mifos.workflow.infrastructure.core.MifosFlowInfrastructureConstants.MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_ERROR_PREFIX;

import java.io.Serial;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mifos.commons.boot.core.exception.MifosBaseException;
import org.mifos.commons.boot.core.model.MifosError;
import org.mifos.commons.boot.core.model.MifosErrorCode;

public class MifosFlowException extends MifosBaseException {
    @Serial
    private static final long serialVersionUID = 1L;

    public MifosFlowException(MifosError error) {
        super(error);
    }

    @Getter
    @RequiredArgsConstructor
    public enum MifosFlowErrorCode implements MifosErrorCode {
        MIFOS_FLOW_ERROR_UNKNOWN(
                MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_START,
                MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_ERROR_PREFIX + ".unknown"),
        MIFOS_FLOW_ERROR_NOT_FOUND(
                MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_START + MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_INCREMENT,
                MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_ERROR_PREFIX + ".not-found"),
        MIFOS_FLOW_ERROR_DEPLOYMENT_FAILED(
                MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_START
                        + (MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_INCREMENT * 2),
                MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_ERROR_PREFIX + ".deployment-failed"),
        MIFOS_FLOW_ERROR_DEPLOYMENT_NOT_FOUND(
                MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_START
                        + (MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_INCREMENT * 3),
                MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_ERROR_PREFIX + ".deployment-not-found"),
        MIFOS_FLOW_ERROR_PROCESS_DEFINITION_NOT_FOUND(
                MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_START
                        + (MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_INCREMENT * 4),
                MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_ERROR_PREFIX + ".process-definition-not-found"),
        MIFOS_FLOW_ERROR_PROCESS_NOT_FOUND(
                MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_START
                        + (MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_INCREMENT * 5),
                MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_ERROR_PREFIX + ".process-not-found"),
        MIFOS_FLOW_ERROR_PROCESS_START_FAILED(
                MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_START
                        + (MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_INCREMENT * 6),
                MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_ERROR_PREFIX + ".process-start-failed"),
        MIFOS_FLOW_ERROR_TASK_NOT_FOUND(
                MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_START
                        + (MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_INCREMENT * 7),
                MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_ERROR_PREFIX + ".task-not-found"),
        MIFOS_FLOW_ERROR_ENGINE_UNAVAILABLE(
                MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_START
                        + (MIFOS_WORKFLOW_INFRASTRUCTURE_ERROR_CODE_INCREMENT * 8),
                MIFOS_WORKFLOW_INFRASTRUCTURE_MESSAGE_ERROR_PREFIX + ".engine-unavailable"),
        ;

        private final int value;
        private final String key;

        @Override
        public String getName() {
            return name();
        }
    }
}
