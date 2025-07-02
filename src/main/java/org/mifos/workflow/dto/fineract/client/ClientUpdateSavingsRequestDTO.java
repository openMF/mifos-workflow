package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Data transfer object for updating a client's savings account.
 */
@Data
@Builder
public class ClientUpdateSavingsRequestDTO {
    private Long savingsAccountId;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("savingsAccountId", savingsAccountId);
        return map;
    }
}