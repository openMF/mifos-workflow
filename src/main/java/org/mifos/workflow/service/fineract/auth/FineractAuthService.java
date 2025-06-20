package org.mifos.workflow.service.fineract.auth;


import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.fineract.client.models.PostAuthenticationRequest;

import org.mifos.workflow.api.auth.AuthenticationApi;
import org.mifos.workflow.dto.fineract.auth.AuthenticationRequest;
import org.mifos.workflow.dto.fineract.auth.AuthenticationResponse;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;

/**
 * Service for handling authentication with the Fineract API.
 * This service provides methods to authenticate users and manage authentication state.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FineractAuthService {

    private final AuthenticationApi authenticationApi;
    @Getter
    private String cachedAuthKey;

    @PostConstruct
    public void init() {
        log.info("Initializing FineractAuthService");
    }

    public Observable<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Authentication request cannot be null");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password cannot be empty");
        }

        log.info("Starting Fineract authentication for user: {}", request.getUsername());

        PostAuthenticationRequest authRequest = new PostAuthenticationRequest(
                request.getPassword(),
                request.getUsername()
        );

        return authenticationApi.authenticate(authRequest, false)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(response -> {
                    if (response == null || !response.getAuthenticated() || 
                        response.getBase64EncodedAuthenticationKey() == null) {
                        throw new RuntimeException("Invalid authentication response");
                    }
                    log.info("Authentication successful!");
                    cachedAuthKey = response.getBase64EncodedAuthenticationKey();
                    return AuthenticationResponse.from(response);
                })
                .doOnError(error -> log.error("Authentication failed: {}", error.getMessage()))
                .doOnComplete(() -> log.info("Authentication request completed"))
                .timeout(5, TimeUnit.SECONDS);
    }
}