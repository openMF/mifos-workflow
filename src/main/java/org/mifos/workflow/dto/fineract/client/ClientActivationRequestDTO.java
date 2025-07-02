package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

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
    @NotNull
    private String dateFormat;
    @NotNull
    private String locale;
    @NotNull
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