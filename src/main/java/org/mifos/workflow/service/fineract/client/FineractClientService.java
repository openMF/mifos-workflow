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
import org.mifos.workflow.dto.fineract.client.AddressDTO;
import org.mifos.workflow.dto.fineract.client.BasicClientCreateRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientActivationRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientCloseRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientCreateRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientRejectRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientTransferRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientUpdateRequestDTO;
import org.mifos.workflow.dto.fineract.client.CodeValueCreateRequestDTO;
import org.mifos.workflow.dto.fineract.code.CodeDataDTO;
import org.springframework.stereotype.Service;
import retrofit2.HttpException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

    private void validateClientRequest(ClientCreateRequestDTO request) {
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

    public Observable<PostClientsResponse> createClient(ClientCreateRequestDTO request, String dateFormat, String locale, Long addressTypeId) {
        validateClientRequest(request);
        log.info("Creating client with name: {}", request.getFirstName() + " " + request.getLastName());

        ClientCreateRequestDTO clientRequest = ClientCreateRequestDTO.builder()
                .dateFormat(dateFormat)
                .locale(locale)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .officeId(request.getOfficeId())
                .active(request.isActive())
                .legalFormId(request.getLegalFormId())
                .mobileNo(request.getMobileNo())
                .dateOfBirth(request.getDateOfBirth())
                .externalId(request.getExternalId())
                .address(getAddressList(request, addressTypeId))
                .build();

        log.info("Sending request to Fineract: {}", clientRequest.toMap());

        return handleError(clientsApi.createClient(clientRequest.toMap()), "client creation");
    }

    @NotNull
    private static List<AddressDTO> getAddressList(ClientCreateRequestDTO request, Long addressTypeId) {
        List<AddressDTO> addressList = new ArrayList<>();
        AddressDTO address = AddressDTO.builder()
                .addressTypeId(addressTypeId)
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .stateProvinceId(request.getStateProvinceId() != null ? Long.parseLong(request.getStateProvinceId()) : null)
                .countryId(request.getCountryId() != null ? Long.parseLong(request.getCountryId()) : null)
                .postalCode(request.getPostalCode())
                .build();
        addressList.add(address);
        return addressList;
    }

    public Observable<PostClientsResponse> createBasicClient(String firstname, String lastname, String mobileNo, Long officeId,
                                                             String dateFormat, String locale, Long legalFormId) {
        if (firstname == null || lastname == null || officeId == null) {
            return Observable.error(new IllegalArgumentException("Mandatory fields (firstname, lastname, officeId) cannot be null"));
        }

        BasicClientCreateRequestDTO basicClientRequest = BasicClientCreateRequestDTO.builder()
                .dateFormat(dateFormat)
                .locale(locale)
                .firstname(firstname)
                .lastname(lastname)
                .mobileNo(mobileNo)
                .officeId(officeId)
                .active(false)
                .legalFormId(legalFormId)
                .submittedOnDate(LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat)))
                .build();

        log.info("Creating basic client with request: {}", basicClientRequest.toMap());

        return handleError(clientsApi.createClient(basicClientRequest.toMap()), "basic client creation");
    }

    public Observable<PostClientsClientIdResponse> activateClient(Long clientId, String dateFormat, String locale) {
        return activateClient(clientId, LocalDate.now(), dateFormat, locale);
    }

    public Observable<PostClientsClientIdResponse> activateClient(Long clientId, LocalDate activationDate, String dateFormat, String locale) {
        if (clientId == null || activationDate == null) {
            return Observable.error(new IllegalArgumentException("Client ID or activation date cannot be null"));
        }
        log.info("Activating client with ID: {}, activation date: {}", clientId, activationDate);

        ClientActivationRequestDTO activationRequest = ClientActivationRequestDTO.builder()
                .dateFormat(dateFormat)
                .locale(locale)
                .activationDate(activationDate)
                .build();

        return handleError(clientsApi.activateClient(clientId, ACTIVATE_COMMAND, activationRequest.toMap(dateFormat)), "client activation");
    }

    public Observable<GetClientsClientIdResponse> retrieveClient(Long clientId) {
        if (clientId == null) {
            return Observable.error(new IllegalArgumentException("Client ID cannot be null"));
        }
        log.info("Retrieving client with ID: {}", clientId);

        return handleError(clientsApi.retrieveClient(clientId), "client retrieval");
    }

    public Observable<PutClientsClientIdResponse> updateClient(Long clientId, ClientUpdateRequestDTO updateRequest) {
        if (clientId == null || updateRequest == null) {
            return Observable.error(new IllegalArgumentException("Client ID or update request cannot be null"));
        }
        log.info("Updating client with ID: {} using update request", clientId);

        return handleError(clientsApi.updateClient(clientId, updateRequest.toMap()), "client update");
    }

    public Observable<PostClientsClientIdResponse> transferClient(Long clientId, String command, ClientTransferRequestDTO transferRequest) {
        if (clientId == null || command == null || transferRequest == null) {
            return Observable.error(new IllegalArgumentException("Client ID, command, or transfer request cannot be null"));
        }
        log.info("Applying command {} to client with ID: {}", command, clientId);

        return handleError(
                clientsApi.retrieveTransferTemplate(clientId)
                        .flatMap(template -> {
                            log.info("Retrieved transfer template for client: {}", clientId);
                            return clientsApi.applyCommand(clientId.toString(), transferRequest.toMap(), command);
                        }),
                "client transfer"
        );
    }

    public Observable<PostClientsClientIdResponse> rejectClient(Long clientId, String command, ClientRejectRequestDTO rejectRequest) {
        if (clientId == null || command == null || rejectRequest == null) {
            return Observable.error(new IllegalArgumentException("Client ID, command, or reject request cannot be null"));
        }
        log.info("Applying command {} to client with ID: {}", command, clientId);

        log.info("Sending reject command request: {}", rejectRequest.toMap());

        return clientsApi.applyCommand(clientId.toString(), rejectRequest.toMap(), command)
                .subscribeOn(Schedulers.io())
                .doOnNext(response -> log.info("Command {} applied successfully to client: {}", command, clientId))
                .doOnError(error -> {
                    log.error("Error applying command {} to client: {}", command, error.getMessage(), error);
                    logHttpErrorDetails(error);
                });
    }

    public Observable<PostClientsClientIdResponse> closeClient(Long clientId, String command, ClientCloseRequestDTO closeRequest) {
        if (clientId == null || command == null || closeRequest == null) {
            return Observable.error(new IllegalArgumentException("Client ID, command, or close request cannot be null"));
        }
        log.info("Applying command {} to client with ID: {}", command, clientId);

        return handleError(clientsApi.applyCommand(clientId.toString(), closeRequest.toMap(), command), "client closure");
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
                                codes.stream().map(CodeDataDTO::getName).collect(Collectors.joining(", ")));
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
                                codes.stream().map(CodeDataDTO::getName).collect(Collectors.joining(", ")));
                        return Observable.error(new IllegalStateException("Client rejection reason code not found in the system"));
                    }

                    log.info("Found client rejection reason code ID: {}", rejectionCodeId);

                    CodeValueCreateRequestDTO codeValueRequest = CodeValueCreateRequestDTO.builder()
                            .name(name)
                            .description(description)
                            .position(1)
                            .isActive(true)
                            .build();

                    return clientsApi.createCodeValue(rejectionCodeId, codeValueRequest.toMap());
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
                                codes.stream().map(CodeDataDTO::getName).collect(Collectors.joining(", ")));
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
                                codes.stream().map(CodeDataDTO::getName).collect(Collectors.joining(", ")));
                        return Observable.error(new IllegalStateException("Client closure reason code not found in the system"));
                    }

                    log.info("Found client closure reason code ID: {}", closureCodeId);

                    CodeValueCreateRequestDTO codeValueRequest = CodeValueCreateRequestDTO.builder()
                            .name(name)
                            .description(description)
                            .position(1)
                            .isActive(true)
                            .build();

                    return clientsApi.createCodeValue(closureCodeId, codeValueRequest.toMap());
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