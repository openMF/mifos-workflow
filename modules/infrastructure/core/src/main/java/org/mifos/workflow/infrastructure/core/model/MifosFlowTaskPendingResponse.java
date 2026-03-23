/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifos.workflow.infrastructure.core.model;

import java.io.Serial;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.mifos.commons.boot.core.model.MifosResponse;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class MifosFlowTaskPendingResponse implements MifosResponse {
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
