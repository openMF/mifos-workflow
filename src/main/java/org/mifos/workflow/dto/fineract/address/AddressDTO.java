package org.mifos.workflow.dto.fineract.address;

import lombok.Builder;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for client addresses.
 */
@Data
@Builder
public class AddressDTO {
    private Long addressTypeId;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private Long stateProvinceId;
    private Long countryId;
    private String postalCode;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("addressTypeId", addressTypeId);
        map.put("addressLine1", addressLine1);
        map.put("addressLine2", addressLine2);
        map.put("city", city);
        map.put("stateProvinceId", stateProvinceId);
        map.put("countryId", countryId);
        map.put("postalCode", postalCode);
        return map;
    }
} 