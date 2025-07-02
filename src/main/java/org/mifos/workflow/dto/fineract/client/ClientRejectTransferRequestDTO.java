package org.mifos.workflow.dto.fineract.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for rejecting a client transfer request in Fineract
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRejectTransferRequestDTO {
    private String dateFormat;
    private String locale;
    private LocalDate rejectionDate;
    private String rejectionReasonId;
    private String note;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("dateFormat", dateFormat);
        map.put("locale", locale);
        if (rejectionDate != null && dateFormat != null) {
            map.put("rejectionDate", rejectionDate.format(java.time.format.DateTimeFormatter.ofPattern(dateFormat)));
        }
        map.put("rejectionReasonId", rejectionReasonId);
        map.put("note", note);
        return map;
    }
} 