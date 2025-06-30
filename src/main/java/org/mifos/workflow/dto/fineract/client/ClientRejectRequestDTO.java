package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;

/**
 * Data transfer object for client rejection requests.
 */
@Data
@Builder
public class ClientRejectRequestDTO {
    private String dateFormat;
    private String locale;
    private LocalDate rejectionDate;
    private Long rejectionReasonId;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (dateFormat != null) map.put("dateFormat", dateFormat);
        if (locale != null) map.put("locale", locale);
        if (rejectionDate != null) {
            String format = dateFormat != null ? dateFormat : "dd MMMM yyyy";
            map.put("rejectionDate", rejectionDate.format(java.time.format.DateTimeFormatter.ofPattern(format)));
        }
        if (rejectionReasonId != null) map.put("rejectionReasonId", rejectionReasonId);
        return map;
    }
} 