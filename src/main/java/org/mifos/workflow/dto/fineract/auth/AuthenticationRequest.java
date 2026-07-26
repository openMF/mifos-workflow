package org.mifos.workflow.dto.fineract.auth;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a request for authentication in the Fineract system.
 * Contains the necessary credentials for user authentication.
 */
@Data
@Builder
public class AuthenticationRequest {

    @NotBlank(message = "Username must not be blank")
    private String username;

    @NotBlank(message = "Password must not be blank")
    private String password;
}
