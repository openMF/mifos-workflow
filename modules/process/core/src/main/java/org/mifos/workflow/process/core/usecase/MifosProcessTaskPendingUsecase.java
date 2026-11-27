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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.mifos.commons.boot.operation.core.usecase.MifosUsecase;

/// Lists pending user tasks.
public interface MifosProcessTaskPendingUsecase
        extends MifosUsecase<MifosProcessTaskPendingUsecase.Request, MifosProcessTaskPendingUsecase.Response> {
    /// Request for pending tasks, optionally filtered by user.
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    class Request implements MifosRequest {
        @Serial
        private static final long serialVersionUID = 1L;

        private String userId;
    }

    /// Response containing the pending tasks.
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    class Response implements MifosResponse {
        @Serial
        private static final long serialVersionUID = 1L;

        private List<Task> tasks;
    }

    /// A pending user task.
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    class Task implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String taskId;
        private String name;
        private String processId;
        private String processDefinitionId;
        private String assignee;
        private LocalDateTime createTime;
        private LocalDateTime dueDate;
        private String description;
        private Integer priority;
    }
}
