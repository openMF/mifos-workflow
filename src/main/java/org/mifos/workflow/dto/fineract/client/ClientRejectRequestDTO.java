package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;

/**
 * Data transfer object for client rejection requests.
 */
@Data
@Builder
public class ClientRejectRequestDTO {
    private static final String DEFAULT_DATE_FORMAT = "dd MMMM yyyy";
    @NotNull
    private String dateFormat;
    @NotNull
    private String locale;
    @NotNull
    private LocalDate rejectionDate;
    @NotNull
    private Long rejectionReasonId;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (dateFormat != null) map.put("dateFormat", dateFormat);
        if (locale != null) map.put("locale", locale);
        if (rejectionDate != null) {
            String format = dateFormat != null ? dateFormat : DEFAULT_DATE_FORMAT;
            map.put("rejectionDate", rejectionDate.format(java.time.format.DateTimeFormatter.ofPattern(format)));
        }
        if (rejectionReasonId != null) map.put("rejectionReasonId", rejectionReasonId);
        return map;
    }
} 