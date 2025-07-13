package org.mifos.workflow.service.fineract.auth;


import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.fineract.client.models.PostAuthenticationRequest;
import org.mifos.workflow.api.auth.AuthenticationApi;
import org.mifos.workflow.dto.fineract.auth.AuthenticationRequest;
import org.mifos.workflow.dto.fineract.auth.AuthenticationResponse;
import org.mifos.workflow.util.FineractErrorHandler;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

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

    private <T> Observable<T> handleError(Observable<T> observable, String operation, String resourceId) {
        return observable
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext(error -> Observable.error(FineractErrorHandler.handleError(operation, error, resourceId)));
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

        return handleError(
                authenticationApi.authenticate(authRequest, false)
                        .observeOn(Schedulers.computation())
                        .map(response -> {
                            if (response == null || !response.getAuthenticated() ||
                                    response.getBase64EncodedAuthenticationKey() == null) {
                                throw new RuntimeException("Invalid authentication response");
                            }
                            log.info("Authentication successful for user: {}", request.getUsername());
                            cachedAuthKey = response.getBase64EncodedAuthenticationKey();
                            return AuthenticationResponse.from(response);
                        })
                        .doOnComplete(() -> log.info("Authentication request completed for user: {}", request.getUsername()))
                        .timeout(5, TimeUnit.SECONDS),
                "authentication",
                request.getUsername()
        );
    }

    public void clearCachedAuthKey() {
        cachedAuthKey = null;
        log.info("Cached authentication key cleared");
    }


    public boolean isAuthenticated() {
        return cachedAuthKey != null && !cachedAuthKey.isEmpty();
    }
}