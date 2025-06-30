package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for client undo withdraw requests.
 */
@Data
@Builder
public class ClientUndoWithdrawRequestDTO {
    private LocalDate reopenedDate;
    private String dateFormat;
    private String locale;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("reopenedDate", reopenedDate.format(DateTimeFormatter.ofPattern(dateFormat)));
        map.put("dateFormat", dateFormat);
        map.put("locale", locale);
        return map;
    }
} 