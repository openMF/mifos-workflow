package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for basic client creation requests.
 */
@Data
@Builder
public class BasicClientCreateRequestDTO {
    private String dateFormat;
    private String locale;
    private String firstname;
    private String lastname;
    private String mobileNo;
    private Long officeId;
    private Boolean active;
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