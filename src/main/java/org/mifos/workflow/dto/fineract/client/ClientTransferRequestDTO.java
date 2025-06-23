package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for client transfer requests.
 */
@Data
@Builder
public class ClientTransferRequestDTO {
    private Object transferDate;
    private Object destinationOfficeId;
    // Add other transfer-specific fields as needed

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (transferDate != null) {
            map.put("transferDate", transferDate);
        }
        if (destinationOfficeId != null) {
            map.put("destinationOfficeId", destinationOfficeId);
        }
        return map;
    }
} 