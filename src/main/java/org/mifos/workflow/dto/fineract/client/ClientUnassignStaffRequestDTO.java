package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for unassigning a staff member to a client
 */
@Data
@Builder
public class ClientUnassignStaffRequestDTO {
    private String dateFormat;
    private String locale;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("dateFormat", dateFormat);
        map.put("locale", locale);
        return map;
    }
} 