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
        ;

        private final int value;
        private final String key;

        @Override
        public String getName() {
            return name();
        }
    }
}
