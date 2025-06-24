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
    private String dateFormat;
    private LocalDate transferDate;
    private Long destinationOfficeId;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (dateFormat != null) map.put("dateFormat", dateFormat);
        if (transferDate != null) {
            String format = dateFormat != null ? dateFormat : "dd MMMM yyyy";
            map.put("transferDate", transferDate.format(java.time.format.DateTimeFormatter.ofPattern(format)));
        }
        if (destinationOfficeId != null) {
            map.put("destinationOfficeId", destinationOfficeId);
        }
        return map;
    }
} 