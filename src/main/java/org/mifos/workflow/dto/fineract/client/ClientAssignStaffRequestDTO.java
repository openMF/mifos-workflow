package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for assigning a staff member to a client
 */
@Data
@Builder
public class ClientAssignStaffRequestDTO {
    private Long staffId;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("staffId", staffId);
        return map;
    }
}