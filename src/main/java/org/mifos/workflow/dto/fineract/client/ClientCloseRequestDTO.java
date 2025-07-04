package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;

/**
 * Data transfer object for client close requests.
 */
@Data
@Builder
public class ClientCloseRequestDTO {
    @NotNull
    private String dateFormat;
    @NotNull
    private String locale;
    @NotNull
    private LocalDate closureDate;
    @NotNull
    private Long closureReasonId;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (dateFormat != null) map.put("dateFormat", dateFormat);
        if (locale != null) map.put("locale", locale);
        if (closureDate != null) {
            String format = dateFormat != null ? dateFormat : "dd MMMM yyyy";
            map.put("closureDate", closureDate.format(java.time.format.DateTimeFormatter.ofPattern(format)));
        }
        if (closureReasonId != null) map.put("closureReasonId", closureReasonId);
        return map;
    }
} 