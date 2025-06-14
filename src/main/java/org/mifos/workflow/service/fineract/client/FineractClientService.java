package org.mifos.workflow.service.fineract.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.PostClientsClientIdRequest;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.mifos.workflow.api.client.ClientApi;
import org.mifos.workflow.dto.fineract.client.ClientCreateRequest;
import org.mifos.workflow.dto.fineract.client.ClientCreateResponse;
import org.mifos.workflow.service.fineract.auth.FineractAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service for managing client operations in the Fineract system.
 * This service handles client creation and activation using the Fineract API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FineractClientService {

    private final ClientApi clientApi;

    @Autowired
    private FineractAuthService authService;

    @PostConstruct
    public void init() {
        log.info("FineractClientService initialized with ClientApi");
    }

    public CompletableFuture<ClientCreateResponse> createClient(ClientCreateRequest request) {
        log.info("Creating client with name: {}", request.getFirstName() + " " + request.getLastName());


        PostClientsRequest apiRequest = new PostClientsRequest()
                .officeId(request.getOfficeId().intValue())
                .fullname(request.getFirstName() + " " + request.getLastName())
                .active(request.isActive())
                .dateFormat("dd MMMM yyyy")
                .locale("en");


        if (request.isActive()) {
            String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            apiRequest.activationDate(formattedDate);
        }

        CompletableFuture<ClientCreateResponse> future = new CompletableFuture<>();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<PostClientsResponse> responseRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();


        clientApi.createClient(apiRequest)
                .subscribe(
                        response -> {
                            responseRef.set(response);
                            latch.countDown();
                        },
                        error -> {
                            log.error("API error details: {}", error.getMessage());
                            errorRef.set(error);
                            latch.countDown();
                        }
                );

        try {
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            if (!completed) {
                future.completeExceptionally(new RuntimeException("Timed out while creating client"));
                return future;
            }

            if (errorRef.get() != null) {
                log.error("Error creating client: {}", errorRef.get().getMessage(), errorRef.get());
                future.completeExceptionally(new RuntimeException("Failed to create client", errorRef.get()));
                return future;
            }

            PostClientsResponse response = responseRef.get();
            ClientCreateResponse result = ClientCreateResponse.builder()
                    .clientId(response.getClientId().longValue())
                    .resourceId(response.getResourceId().longValue())
                    .build();

            log.info("Client created successfully with ID: {}", result.getClientId());
            future.complete(result);
        } catch (Exception e) {
            log.error("Error creating client: {}", e.getMessage(), e);
            future.completeExceptionally(new RuntimeException("Failed to create client", e));
        }

        return future;
    }

    public CompletableFuture<Void> activateClient(Long clientId) {
        log.info("Activating client with ID: {}", clientId);
        CompletableFuture<Void> future = new CompletableFuture<>();

        PostClientsClientIdRequest activationRequest = new PostClientsClientIdRequest()
                .locale("en")
                .dateFormat("dd MMMM yyyy")
                .activationDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();


        clientApi.activateClient(clientId, activationRequest)
                .subscribe(
                        response -> latch.countDown(),
                        error -> {
                            log.error("API error during activation: {}", error.getMessage());
                            errorRef.set(error);
                            latch.countDown();
                        }
                );

        try {
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            if (!completed) {
                future.completeExceptionally(new RuntimeException("Timed out while activating client"));
                return future;
            }

            if (errorRef.get() != null) {
                log.error("Error activating client: {}", errorRef.get().getMessage(), errorRef.get());
                future.completeExceptionally(new RuntimeException("Failed to activate client", errorRef.get()));
                return future;
            }

            log.info("Client activated successfully with ID: {}", clientId);
            future.complete(null);
        } catch (Exception e) {
            log.error("Error activating client: {}", e.getMessage(), e);
            future.completeExceptionally(new RuntimeException("Failed to activate client", e));
        }

        return future;
    }
}