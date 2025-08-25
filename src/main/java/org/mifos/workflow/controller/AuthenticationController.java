package org.mifos.workflow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.dto.fineract.auth.AuthenticationRequest;
import org.mifos.workflow.dto.fineract.auth.AuthenticationResponse;
import org.mifos.workflow.dto.fineract.auth.AuthenticationStatusDTO;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.service.fineract.auth.FineractAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.mifos.workflow.util.ApiResponse;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST Controller for handling authentication with Fineract.
 * Provides endpoints for authenticating and checking authentication status.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final FineractAuthService fineractAuthService;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {

        log.info("Authentication request received for user: {}", request.getUsername());

        try {
            AuthenticationResponse response = fineractAuthService.authenticate(request).blockingFirst();
            log.info("Authentication successful for user: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid authentication request for user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (FineractApiException e) {
            log.error("Fineract API error during authentication for user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Unexpected error during authentication for user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status")
    public ResponseEntity<AuthenticationStatusDTO> getAuthenticationStatus() {
        boolean isAuthenticated = fineractAuthService.isAuthenticated();

        AuthenticationStatusDTO status = AuthenticationStatusDTO.builder().authenticated(isAuthenticated).build();

        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("Logout request received");
        fineractAuthService.clearCachedAuthKey();
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }


} 