package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for client activation requests.
 */
@Data
@Builder
public class ClientActivationRequestDTO {
    private String dateFormat;
    private String locale;
    private LocalDate activationDate;

    public Map<String, Object> toMap(String dateFormat) {
        Map<String, Object> map = new HashMap<>();
        if (this.dateFormat != null) map.put("dateFormat", this.dateFormat);
        if (locale != null) map.put("locale", locale);
        if (activationDate != null) {
            map.put("activationDate", activationDate.format(DateTimeFormatter.ofPattern(dateFormat)));
        }
        return map;
    }
} 