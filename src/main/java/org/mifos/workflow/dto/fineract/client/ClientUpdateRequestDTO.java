package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for client update.
 */
@Data
@Builder
public class ClientUpdateRequestDTO {
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private String mobileNo;
    @NotNull
    private String externalId;
    @NotNull
    private Boolean active;
    @NotNull
    private Long officeId;
    @NotNull
    private Long legalFormId;
    @NotNull
    private String dateFormat;
    @NotNull
    private String locale;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (firstName != null) map.put("firstname", firstName);
        if (lastName != null) map.put("lastname", lastName);
        if (mobileNo != null) map.put("mobileNo", mobileNo);
        if (externalId != null) map.put("externalId", externalId);
        if (active != null) map.put("active", active);
        if (officeId != null) map.put("officeId", officeId);
        if (legalFormId != null) map.put("legalFormId", legalFormId);
        if (dateFormat != null) map.put("dateFormat", dateFormat);
        if (locale != null) map.put("locale", locale);
        return map;
    }
} 