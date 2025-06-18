package org.mifos.workflow.service.fineract.client;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.mifos.fineract.client.models.GetClientsClientIdResponse;
import org.mifos.fineract.client.models.GetClientsResponse;
import org.mifos.fineract.client.models.PostClientsClientIdResponse;
import org.mifos.fineract.client.models.PostClientsResponse;
import org.mifos.fineract.client.models.PutClientsClientIdResponse;
import org.mifos.workflow.api.client.ClientsApi;
import org.mifos.workflow.dto.fineract.client.ClientCreateRequest;
import org.mifos.workflow.dto.fineract.code.CodeData;
import org.springframework.stereotype.Service;
import retrofit2.HttpException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mifos.workflow.dto.fineract.office.OfficeDTO;
import org.mifos.fineract.client.models.CodeValueData;

@Service
@RequiredArgsConstructor
@Slf4j
public class FineractClientService {


    private static final String ACTIVATE_COMMAND = "activate";

    public final ClientsApi clientsApi;


    @PostConstruct
    public void init() {
        log.info("FineractClientService initialized with ClientsApi");
    }

    private <T> Observable<T> handleError(Observable<T> observable, String operation) {
        return observable
                .subscribeOn(Schedulers.io())
                .doOnError(error -> {
                    log.error("Error during {}: {}", operation, error.getMessage(), error);
                    logHttpErrorDetails(error);
                });
    }

    private void validateClientRequest(ClientCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Client creation request cannot be null");
        }
        if (request.getOfficeId() == null) {
            throw new IllegalArgumentException("Office ID cannot be null");
        }
        if (request.getLegalFormId() == null) {
            throw new IllegalArgumentException("Legal form ID cannot be null");
        }
    }

    private Map<String, Object> createBaseRequestMap(String dateFormat, String locale) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("dateFormat", dateFormat);
        requestMap.put("locale", locale);
        return requestMap;
    }

    public Observable<PostClientsResponse> createClient(ClientCreateRequest request, String dateFormat, String locale, Long addressTypeId) {
        validateClientRequest(request);
        log.info("Creating client with name: {}", request.getFirstName() + " " + request.getLastName());

        Map<String, Object> requestMap = createBaseRequestMap(dateFormat, locale);
        requestMap.put("firstname", request.getFirstName());
        requestMap.put("lastname", request.getLastName());
        requestMap.put("officeId", request.getOfficeId());
        requestMap.put("active", request.isActive());
        requestMap.put("legalFormId", request.getLegalFormId());
        requestMap.put("mobileNo", request.getMobileNo());

        if (request.getDateOfBirth() != null) {
            requestMap.put("dateOfBirth", request.getDateOfBirth().format(DateTimeFormatter.ofPattern(dateFormat)));
        }

        requestMap.put("externalId", request.getExternalId());
        requestMap.put("address", getMaps(request, addressTypeId));
        requestMap.put("submittedOnDate", LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat)));

        log.info("Sending request to Fineract: {}", requestMap);

        return handleError(clientsApi.createClient(requestMap), "client creation");
    }

    @NotNull
    private static List<Map<String, Object>> getMaps(ClientCreateRequest request, Long addressTypeId) {
        List<Map<String, Object>> addressList = new ArrayList<>();
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("addressTypeId", addressTypeId);
        addressMap.put("addressLine1", request.getAddressLine1());
        addressMap.put("addressLine2", request.getAddressLine2());
        addressMap.put("city", request.getCity());
        addressMap.put("stateProvinceId", request.getStateProvinceId() != null ? Long.parseLong(request.getStateProvinceId()) : null);
        addressMap.put("countryId", request.getCountryId() != null ? Long.parseLong(request.getCountryId()) : null);
        addressMap.put("postalCode", request.getPostalCode());
        addressList.add(addressMap);
        return addressList;
    }

    public Observable<PostClientsResponse> createBasicClient(String firstname, String lastname, String mobileNo, Long officeId,
                                                             String dateFormat, String locale, Long legalFormId) {
        if (firstname == null || lastname == null || officeId == null) {
            return Observable.error(new IllegalArgumentException("Mandatory fields (firstname, lastname, officeId) cannot be null"));
        }

        Map<String, Object> requestMap = createBaseRequestMap(dateFormat, locale);
        requestMap.put("firstname", firstname);
        requestMap.put("lastname", lastname);
        requestMap.put("mobileNo", mobileNo);
        requestMap.put("officeId", officeId);
        requestMap.put("active", false);
        requestMap.put("legalFormId", legalFormId);
        requestMap.put("submittedOnDate", LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat)));

        log.info("Creating basic client with request: {}", requestMap);

        return handleError(clientsApi.createClient(requestMap), "basic client creation");
    }

    public Observable<PostClientsClientIdResponse> activateClient(Long clientId, String dateFormat, String locale) {
        return activateClient(clientId, LocalDate.now(), dateFormat, locale);
    }

    public Observable<PostClientsClientIdResponse> activateClient(Long clientId, LocalDate activationDate, String dateFormat, String locale) {
        if (clientId == null || activationDate == null) {
            return Observable.error(new IllegalArgumentException("Client ID or activation date cannot be null"));
        }
        log.info("Activating client with ID: {}, activation date: {}", clientId, activationDate);

        Map<String, Object> request = createBaseRequestMap(dateFormat, locale);
        request.put("activationDate", activationDate.format(DateTimeFormatter.ofPattern(dateFormat)));

        return handleError(clientsApi.activateClient(clientId, ACTIVATE_COMMAND, request), "client activation");
    }

    public Observable<GetClientsClientIdResponse> retrieveClient(Long clientId) {
        if (clientId == null) {
            return Observable.error(new IllegalArgumentException("Client ID cannot be null"));
        }
        log.info("Retrieving client with ID: {}", clientId);

        return handleError(clientsApi.retrieveClient(clientId), "client retrieval");
    }

    public Observable<PutClientsClientIdResponse> updateClientWithMap(Long clientId, Map<String, Object> updateData) {
        if (clientId == null || updateData == null) {
            return Observable.error(new IllegalArgumentException("Client ID or update data cannot be null"));
        }
        log.info("Updating client with ID: {} using map data", clientId);

        return handleError(clientsApi.updateClient(clientId, updateData), "client update");
    }

    public Observable<PostClientsClientIdResponse> transferClient(Long clientId, String command, Map<String, Object> request) {
        if (clientId == null || command == null || request == null) {
            return Observable.error(new IllegalArgumentException("Client ID, command, or request cannot be null"));
        }
        log.info("Applying command {} to client with ID: {}", command, clientId);

        return handleError(
                clientsApi.retrieveTransferTemplate(clientId)
                        .flatMap(template -> {
                            log.info("Retrieved transfer template for client: {}", clientId);
                            Map<String, Object> commandRequest = new HashMap<>(request);
                            if (commandRequest.containsKey("transferDate")) {
                                commandRequest.put("proposedTransferDate", commandRequest.remove("transferDate"));
                            }
                            return clientsApi.applyCommand(clientId.toString(), commandRequest, command);
                        }),
                "client transfer"
        );
    }

    public Observable<PostClientsClientIdResponse> rejectClient(Long clientId, String command, Map<String, Object> request, String dateFormat, String locale) {
        if (clientId == null || command == null || request == null) {
            return Observable.error(new IllegalArgumentException("Client ID, command, or request cannot be null"));
        }
        log.info("Applying command {} to client with ID: {}", command, clientId);

        Map<String, Object> commandRequest = new HashMap<>();
        commandRequest.put("rejectionDate", request.get("rejectionDate"));
        commandRequest.put("rejectionReasonId", request.get("rejectionReasonId"));
        commandRequest.put("dateFormat", dateFormat);
        commandRequest.put("locale", locale);

        log.info("Sending reject command request: {}", commandRequest);

        return clientsApi.applyCommand(clientId.toString(), commandRequest, command)
                .subscribeOn(Schedulers.io())
                .doOnNext(response -> log.info("Command {} applied successfully to client: {}", command, clientId))
                .doOnError(error -> {
                    log.error("Error applying command {} to client: {}", command, error.getMessage(), error);
                    logHttpErrorDetails(error);
                });
    }

    public Observable<PostClientsClientIdResponse> closeClient(Long clientId, String command, Map<String, Object> request, String dateFormat, String locale) {
        if (clientId == null || command == null || request == null) {
            return Observable.error(new IllegalArgumentException("Client ID, command, or request cannot be null"));
        }
        log.info("Applying command {} to client with ID: {}", command, clientId);

        Map<String, Object> commandRequest = new HashMap<>(request);
        if (!commandRequest.containsKey("dateFormat")) {
            commandRequest.put("dateFormat", dateFormat);
        }
        if (!commandRequest.containsKey("locale")) {
            commandRequest.put("locale", locale);
        }

        return handleError(clientsApi.applyCommand(clientId.toString(), commandRequest, command), "client closure");
    }

    public Observable<GetClientsResponse> retrieveAllClients(Long officeId, String searchText, String status, Integer limit, Integer offset) {
        log.info("Retrieving all clients with filters - officeId: {}, searchText: {}, status: {}, limit: {}, offset: {}",
                officeId, searchText, status, limit, offset);

        return handleError(clientsApi.retrieveAllClients(officeId, searchText, status, limit, offset), "client retrieval");
    }

    public Observable<List<OfficeDTO>> retrieveAllOffices() {
        log.info("Retrieving all offices");

        return handleError(clientsApi.retrieveAllOffices(), "office retrieval");
    }

    public Observable<List<CodeValueData>> retrieveClientRejectionReasons() {
        log.info("Retrieving client rejection reasons");


        return clientsApi.retrieveCodes()
                .subscribeOn(Schedulers.io())
                .doOnNext(codes -> {
                    log.info("Retrieved {} codes from the system", codes.size());
                    codes.forEach(code -> log.info("Found code: {} (ID: {})", code.getName(), code.getId()));
                })
                .flatMap(codes -> {
                    Long rejectionCodeId = codes.stream()
                            .filter(code -> "ClientRejectReason".equals(code.getName()))
                            .findFirst()
                            .map(code -> code.getId())
                            .orElse(null);

                    if (rejectionCodeId == null) {
                        log.error("Code 'ClientRejectReason' not found in the system. Available codes: {}",
                                codes.stream().map(CodeData::getName).collect(Collectors.joining(", ")));
                        return Observable.error(new IllegalStateException("Client rejection reason code not found in the system"));
                    }

                    log.info("Found client rejection reason code ID: {}", rejectionCodeId);
                    return clientsApi.retrieveAllCodeValues(rejectionCodeId);
                })
                .doOnNext(response -> {
                    if (response != null && !response.isEmpty()) {
                        log.info("Retrieved {} rejection reasons successfully", response.size());
                        response.forEach(reason -> log.info("Rejection reason: {} (ID: {})", reason.getName(), reason.getId()));
                    } else {
                        log.error("No rejection reasons found for code 'ClientRejectReason'. Please add rejection reasons in the system.");
                        throw new IllegalStateException("No rejection reasons found in the system. Please add rejection reasons first.");
                    }
                })
                .doOnError(error -> {
                    log.error("Error retrieving rejection reasons: {}", error.getMessage(), error);
                    logHttpErrorDetails(error);
                });
    }

    public Observable<CodeValueData> createRejectionReason(String name, String description) {
        log.info("Creating rejection reason with name: {}", name);

        return clientsApi.retrieveCodes()
                .subscribeOn(Schedulers.io())
                .flatMap(codes -> {
                    Long rejectionCodeId = codes.stream()
                            .filter(code -> "ClientRejectReason".equals(code.getName()))
                            .findFirst()
                            .map(code -> code.getId())
                            .orElse(null);

                    if (rejectionCodeId == null) {
                        log.error("Code 'ClientRejectReason' not found in the system. Available codes: {}",
                                codes.stream().map(CodeData::getName).collect(Collectors.joining(", ")));
                        return Observable.error(new IllegalStateException("Client rejection reason code not found in the system"));
                    }

                    log.info("Found client rejection reason code ID: {}", rejectionCodeId);


                    Map<String, Object> request = new HashMap<>();
                    request.put("name", name);
                    request.put("description", description);
                    request.put("position", 1);
                    request.put("isActive", true);

                    return clientsApi.createCodeValue(rejectionCodeId, request);
                })
                .doOnNext(response -> {
                    if (response != null) {
                        log.info("Created rejection reason successfully with ID: {}", response.getId());
                    } else {
                        log.warn("No response received when creating rejection reason");
                    }
                })
                .doOnError(error -> {
                    log.error("Error creating rejection reason: {}", error.getMessage(), error);
                    logHttpErrorDetails(error);
                });
    }

    public Observable<List<CodeValueData>> retrieveClientClosureReasons() {
        log.info("Retrieving client closure reasons");

        return clientsApi.retrieveCodes()
                .subscribeOn(Schedulers.io())
                .doOnNext(codes -> {
                    log.info("Retrieved {} codes from the system", codes.size());
                    codes.forEach(code -> log.info("Found code: {} (ID: {})", code.getName(), code.getId()));
                })
                .flatMap(codes -> {
                    Long closureCodeId = codes.stream()
                            .filter(code -> "ClientClosureReason".equals(code.getName()))
                            .findFirst()
                            .map(code -> code.getId())
                            .orElse(null);

                    if (closureCodeId == null) {
                        log.error("Code 'ClientClosureReason' not found in the system. Available codes: {}",
                                codes.stream().map(CodeData::getName).collect(Collectors.joining(", ")));
                        return Observable.error(new IllegalStateException("Client closure reason code not found in the system"));
                    }

                    log.info("Found client closure reason code ID: {}", closureCodeId);
                    return clientsApi.retrieveAllCodeValues(closureCodeId);
                })
                .doOnNext(response -> {
                    if (response != null && !response.isEmpty()) {
                        log.info("Retrieved {} closure reasons successfully", response.size());
                        response.forEach(reason -> log.info("Closure reason: {} (ID: {})", reason.getName(), reason.getId()));
                    } else {
                        log.error("No closure reasons found for code 'ClientClosureReason'. Please add closure reasons in the system.");
                        throw new IllegalStateException("No closure reasons found in the system. Please add closure reasons first.");
                    }
                })
                .doOnError(error -> {
                    log.error("Error retrieving closure reasons: {}", error.getMessage(), error);
                    logHttpErrorDetails(error);
                });
    }

    public Observable<CodeValueData> createClosureReason(String name, String description) {
        log.info("Creating closure reason with name: {}", name);

        return clientsApi.retrieveCodes()
                .subscribeOn(Schedulers.io())
                .flatMap(codes -> {

                    Long closureCodeId = codes.stream()
                            .filter(code -> "ClientClosureReason".equals(code.getName()))
                            .findFirst()
                            .map(code -> code.getId())
                            .orElse(null);

                    if (closureCodeId == null) {
                        log.error("Code 'ClientClosureReason' not found in the system. Available codes: {}",
                                codes.stream().map(CodeData::getName).collect(Collectors.joining(", ")));
                        return Observable.error(new IllegalStateException("Client closure reason code not found in the system"));
                    }

                    log.info("Found client closure reason code ID: {}", closureCodeId);


                    Map<String, Object> request = new HashMap<>();
                    request.put("name", name);
                    request.put("description", description);
                    request.put("position", 1);
                    request.put("isActive", true);

                    return clientsApi.createCodeValue(closureCodeId, request);
                })
                .doOnNext(response -> {
                    if (response != null) {
                        log.info("Created closure reason successfully with ID: {}", response.getId());
                    } else {
                        log.warn("No response received when creating closure reason");
                    }
                })
                .doOnError(error -> {
                    log.error("Error creating closure reason: {}", error.getMessage(), error);
                    logHttpErrorDetails(error);
                });
    }

    private void logHttpErrorDetails(Throwable error) {
        if (error instanceof HttpException) {
            HttpException httpError = (HttpException) error;
            try {
                String errorBody = httpError.response().errorBody().string();
                log.error("Error response details: {}", errorBody);
            } catch (Exception e) {
                log.error("Could not read error response body", e);
            }
        }
    }
}