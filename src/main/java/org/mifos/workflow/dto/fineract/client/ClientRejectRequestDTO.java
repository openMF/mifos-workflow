package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for client rejection requests.
 */
@Data
@Builder
public class ClientRejectRequestDTO {
    private String dateFormat;
    private String locale;
    private Object rejectionDate;
    private Object rejectionReasonId;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (dateFormat != null) map.put("dateFormat", dateFormat);
        if (locale != null) map.put("locale", locale);
        if (rejectionDate != null) map.put("rejectionDate", rejectionDate);
        if (rejectionReasonId != null) map.put("rejectionReasonId", rejectionReasonId);
        return map;
    }
} 