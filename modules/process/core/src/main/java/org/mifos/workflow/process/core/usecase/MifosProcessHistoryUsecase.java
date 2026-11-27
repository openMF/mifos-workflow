///
/// This Source Code Form is subject to the terms of the Mozilla Public
/// License, v. 2.0. If a copy of the MPL was not distributed with this
/// file, You can obtain one at http://mozilla.org/MPL/2.0/.
///
package org.mifos.workflow.process.core.usecase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.mifos.commons.boot.operation.core.usecase.MifosUsecase;

/// Lists the history of finished workflow process instances.
public interface MifosProcessHistoryUsecase
        extends MifosUsecase<MifosProcessHistoryUsecase.Request, MifosProcessHistoryUsecase.Response> {
    /// A single finished process instance in the process history.
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    class MifosFlowHistoryEntry implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String processId;
        private String processDefinitionId;
        private String processDefinitionKey;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long durationInMillis;
        private String startUserId;
        private String deleteReason;
    }

    /// Request for the process history query.
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    class Request implements MifosRequest {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID id;
    }

    /// Response containing the finished process history entries.
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    class Response implements MifosResponse {
        @Serial
        private static final long serialVersionUID = 1L;

        private List<MifosFlowHistoryEntry> entries;
    }
}
