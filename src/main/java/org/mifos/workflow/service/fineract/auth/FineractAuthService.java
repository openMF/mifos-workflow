package org.mifos.workflow.service.fineract.auth;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.PostAuthenticationRequest;
import org.mifos.workflow.api.auth.AuthenticationApi;
import org.mifos.workflow.dto.fineract.auth.AuthenticationRequest;
import org.mifos.workflow.dto.fineract.auth.AuthenticationResponse;
import org.springframework.stereotype.Service;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
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
        log.info("FineractAuthService initialized");
    }

    public CompletableFuture<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        log.info("Starting Fineract authentication for user: {}", request.getUsername());

        PostAuthenticationRequest authRequest = new PostAuthenticationRequest()
                .username(request.getUsername())
                .password(request.getPassword());

        CompletableFuture<AuthenticationResponse> future = new CompletableFuture<>();
        CountDownLatch latch = new CountDownLatch(1);

        authenticationApi.authenticate(authRequest, false)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            log.info("Authentication successful!");
                            cachedAuthKey = response.getBase64EncodedAuthenticationKey();
                            future.complete(AuthenticationResponse.from(response));
                            latch.countDown();
                        },
                        throwable -> {
                            log.error("Authentication failed: {}", throwable.getMessage(), throwable);
                            future.completeExceptionally(new RuntimeException(throwable));
                            latch.countDown();
                        },
                        () -> {
                            log.info("Authentication request completed");
                            latch.countDown();
                        }
                );

        try {
            if (!latch.await(30, TimeUnit.SECONDS)) {
                String errorMsg = "Authentication operation timed out";
                log.error(errorMsg);
                future.completeExceptionally(new RuntimeException(errorMsg));
            }
        } catch (InterruptedException e) {
            log.error("Wait interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
            future.completeExceptionally(new RuntimeException(e));
        }

        return future;
    }

}