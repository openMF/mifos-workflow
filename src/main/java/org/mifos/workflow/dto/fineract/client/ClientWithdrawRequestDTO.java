package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/*
 * Data transfer object for client withdraw requests.
 */
@Data
@Builder
public class ClientWithdrawRequestDTO {
    private LocalDate withdrawalDate;
    private Long withdrawalReasonId;
    private String dateFormat;
    private String locale;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("withdrawalDate", withdrawalDate.format(DateTimeFormatter.ofPattern(dateFormat)));
        map.put("withdrawalReasonId", withdrawalReasonId);
        map.put("dateFormat", dateFormat);
        map.put("locale", locale);
        return map;
    }
}