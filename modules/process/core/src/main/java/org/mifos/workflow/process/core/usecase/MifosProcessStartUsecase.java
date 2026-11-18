///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.core.usecase;

import java.io.Serial;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.mifos.commons.boot.operation.core.usecase.MifosUsecase;

/// Starts a new workflow process instance.
public interface MifosProcessStartUsecase
        extends MifosUsecase<MifosProcessStartUsecase.Request, MifosProcessStartUsecase.Response> {
    /// Request identifying the process definition to start and its variables.
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    class Request implements MifosRequest {
        @Serial
        private static final long serialVersionUID = 1L;

        private String key;
        private Map<String, Object> variables;
    }

    /// Response containing the id of the started process.
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    class Response implements MifosResponse {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID id;
    }
}
