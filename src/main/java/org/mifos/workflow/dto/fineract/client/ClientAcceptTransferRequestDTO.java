package org.mifos.workflow.dto.fineract.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for accepting a client accept transfer request in Fineract
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAcceptTransferRequestDTO {
    private String dateFormat;
    private String locale;
    private LocalDate transferDate;
    private String note;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("dateFormat", dateFormat);
        map.put("locale", locale);
        if (transferDate != null && dateFormat != null) {
            map.put("transferDate", transferDate.format(java.time.format.DateTimeFormatter.ofPattern(dateFormat)));
        }
        map.put("note", note);
        return map;
    }
} 