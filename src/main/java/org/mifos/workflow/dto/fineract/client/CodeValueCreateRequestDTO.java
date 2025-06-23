package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for creating code values (rejection reasons, closure reasons, etc.).
 */
@Data
@Builder
public class CodeValueCreateRequestDTO {
    private String name;
    private String description;
    private Integer position;
    private Boolean isActive;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("description", description);
        map.put("position", position != null ? position : 1);
        map.put("isActive", isActive != null ? isActive : true);
        return map;
    }
} 