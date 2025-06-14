package org.mifos.workflow.dto.fineract.auth;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a request for authentication in the Fineract system.
 * Contains the necessary credentials for user authentication.
 */
@Data
@Builder
public class AuthenticationRequest {
    private String username;
    private String password;
}