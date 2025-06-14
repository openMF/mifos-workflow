package org.mifos.workflow.dto.fineract.auth;

import lombok.Builder;
import lombok.Data;
import org.apache.fineract.client.models.PostAuthenticationResponse;

/**
 * Represents the response from the Fineract authentication API.
 * This class encapsulates the authentication response data.
 */
@Data
@Builder
public class AuthenticationResponse {
    private String base64EncodedAuthenticationKey;
    private boolean authenticated;
    private String username;

    public static AuthenticationResponse from(PostAuthenticationResponse response) {
        return AuthenticationResponse.builder()
                .base64EncodedAuthenticationKey(response.getBase64EncodedAuthenticationKey())
                .authenticated(Boolean.TRUE.equals(response.getAuthenticated()))
                .username(response.getUsername())
                .build();
    }
}
