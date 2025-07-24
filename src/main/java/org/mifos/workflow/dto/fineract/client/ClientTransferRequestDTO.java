package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;

/**
 * Data transfer object for client transfer requests.
 */
@Data
@Builder
public class ClientTransferRequestDTO {
    private static final String DEFAULT_DATE_FORMAT = "dd MMMM yyyy";
    private String dateFormat;
    private String locale;
    private LocalDate transferDate;
    private Long destinationOfficeId;
    private Long clientId;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (dateFormat != null) map.put("dateFormat", dateFormat);
        if (locale != null) map.put("locale", locale);
        if (transferDate != null) {
            String format = dateFormat != null ? dateFormat : DEFAULT_DATE_FORMAT;
            map.put("transferDate", transferDate.format(java.time.format.DateTimeFormatter.ofPattern(format)));
        }
        if (destinationOfficeId != null) {
            map.put("destinationOfficeId", destinationOfficeId);
        }
        return map;
    }
} 