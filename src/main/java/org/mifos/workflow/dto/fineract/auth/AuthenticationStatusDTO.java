package org.mifos.workflow.dto.fineract.auth;

import lombok.Builder;
import lombok.Data;

/**
 * representing the authentication status of the current session.
 */
@Data
@Builder
public class AuthenticationStatusDTO {
    private boolean authenticated;
} 