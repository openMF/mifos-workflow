package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for client close requests.
 */
@Data
@Builder
public class ClientCloseRequestDTO {
    private String dateFormat;
    private String locale;
    private Object closureDate;
    private Object closureReasonId;
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (dateFormat != null) map.put("dateFormat", dateFormat);
        if (locale != null) map.put("locale", locale);
        if (closureDate != null) map.put("closureDate", closureDate);
        if (closureReasonId != null) map.put("closureReasonId", closureReasonId);
        return map;
    }
} 