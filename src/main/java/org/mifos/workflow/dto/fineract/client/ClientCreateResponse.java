package org.mifos.workflow.dto.fineract.client;

import lombok.Builder;
import lombok.Data;

// Represents the response after creating a client in the Fineract system.
@Data
@Builder
public class ClientCreateResponse {
    private Long clientId;
    private Long resourceId;
}