package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for basic client creation requests.
 */
@Data
@Builder
public class BasicClientCreateRequestDTO {
    @NotNull
    private String dateFormat;
    @NotNull
    private String locale;
    @NotNull
    private String firstname;
    @NotNull
    private String lastname;
    private String mobileNo;
    @NotNull
    private Long officeId;
    @NotNull
    private Boolean active;
    @NotNull
    private Long legalFormId;
    private String submittedOnDate;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (dateFormat != null) map.put("dateFormat", dateFormat);
        if (locale != null) map.put("locale", locale);
        if (firstname != null) map.put("firstname", firstname);
        if (lastname != null) map.put("lastname", lastname);
        if (mobileNo != null) map.put("mobileNo", mobileNo);
        if (officeId != null) map.put("officeId", officeId);
        if (active != null) map.put("active", active);
        if (legalFormId != null) map.put("legalFormId", legalFormId);
        if (submittedOnDate != null) map.put("submittedOnDate", submittedOnDate);
        return map;
    }
} 