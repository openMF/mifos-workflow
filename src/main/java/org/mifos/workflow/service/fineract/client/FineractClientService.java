package org.mifos.workflow.service.fineract.client;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.mifos.fineract.client.models.DeleteClientsClientIdResponse;
import org.mifos.fineract.client.models.GetClientsClientIdResponse;
import org.mifos.fineract.client.models.GetClientsResponse;
import org.mifos.fineract.client.models.PostClientsClientIdResponse;
import org.mifos.fineract.client.models.PostClientsResponse;
import org.mifos.fineract.client.models.PutClientsClientIdResponse;
import org.mifos.workflow.api.client.ClientsApi;
import org.mifos.workflow.dto.fineract.address.AddressDTO;
import org.mifos.workflow.dto.fineract.client.BasicClientCreateRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientAcceptTransferRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientActivationRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientAssignStaffRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientCloseRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientCreateRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientRejectRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientRejectTransferRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientTransferRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientUndoRejectRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientUndoWithdrawRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientUnassignStaffRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientUpdateRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientUpdateSavingsRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientWithdrawRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientWithdrawTransferRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientReactivateRequestDTO;
import org.mifos.workflow.dto.fineract.client.CodeValueCreateRequestDTO;
import org.mifos.workflow.dto.fineract.code.CodeDataDTO;
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
import org.mifos.fineract.client.models.StaffData;
import org.mifos.fineract.client.models.GetClientsClientIdAccountsResponse;
import java.util.Objects;

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

    private void requireParamsNonNull(Map<String, Object> params) {
        params.forEach((name, value) -> {
            Objects.requireNonNull(value, name + " cannot be null");
        });
    }

    public Observable<PostClientsResponse> createClient(@Valid ClientCreateRequestDTO request, String dateFormat, String locale, Long addressTypeId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("request", request);
            params.put("dateFormat", dateFormat);
            params.put("locale", locale);
            params.put("addressTypeId", addressTypeId);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        validateClientRequest(request);
        log.info("Creating client with name: {}", request.getFirstName() + " " + request.getLastName());

        ClientCreateRequestDTO clientRequest = ClientCreateRequestDTO.builder()
                .dateFormat(dateFormat)
                .locale(locale)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .officeId(request.getOfficeId())
                .active(request.getActive())
                .legalFormId(request.getLegalFormId())
                .mobileNo(request.getMobileNo())
                .dateOfBirth(request.getDateOfBirth())
                .externalId(request.getExternalId())
                .address(getAddressList(request, addressTypeId))
                .submissionDate(LocalDate.now())
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
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("firstname", firstname);
            params.put("lastname", lastname);
            params.put("officeId", officeId);
            params.put("dateFormat", dateFormat);
            params.put("locale", locale);
            params.put("legalFormId", legalFormId);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
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
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("dateFormat", dateFormat);
            params.put("locale", locale);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        return activateClient(clientId, LocalDate.now(), dateFormat, locale);
    }

    public Observable<PostClientsClientIdResponse> activateClient(Long clientId, LocalDate activationDate, String dateFormat, String locale) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("activationDate", activationDate);
            params.put("dateFormat", dateFormat);
            params.put("locale", locale);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
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
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Retrieving client with ID: {}", clientId);

        return handleError(clientsApi.retrieveClient(clientId), "client retrieval");
    }

    public Observable<PutClientsClientIdResponse> updateClient(Long clientId, @Valid ClientUpdateRequestDTO updateRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("updateRequest", updateRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Updating client with ID: {} using update request", clientId);

        return handleError(clientsApi.updateClient(clientId, updateRequest.toMap()), "client update");
    }

    public Observable<PostClientsClientIdResponse> transferClient(Long clientId, String command, @Valid ClientTransferRequestDTO transferRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("command", command);
            params.put("transferRequest", transferRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
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

    public Observable<PostClientsClientIdResponse> rejectClient(Long clientId, String command, @Valid ClientRejectRequestDTO rejectRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("command", command);
            params.put("rejectRequest", rejectRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Applying command {} to client with ID: {}", command, clientId);

        log.info("Sending reject command request: {}", rejectRequest.toMap());

        return handleError(
                clientsApi.applyCommand(clientId.toString(), rejectRequest.toMap(), command)
                        .subscribeOn(Schedulers.io())
                        .doOnNext(response -> log.info("Command {} applied successfully to client: {}", command, clientId))
                        .doOnError(error -> {
                            log.error("Error applying command {} to client: {}", command, error.getMessage(), error);
                            logHttpErrorDetails(error);
                        }),
                "client reject"
        );
    }

    public Observable<PostClientsClientIdResponse> closeClient(Long clientId, String command, @Valid ClientCloseRequestDTO closeRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("command", command);
            params.put("closeRequest", closeRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Applying command {} to client with ID: {}", command, clientId);
        return handleError(clientsApi.applyCommand(clientId.toString(), closeRequest.toMap(), command), "client closure");
    }

    public Observable<GetClientsResponse> retrieveAllClients(Long officeId, String searchText, String status, Integer limit, Integer offset) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("officeId", officeId);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Retrieving all clients with filters - officeId: {}, searchText: {}, status: {}, limit: {}, offset: {}",
                officeId, searchText, status, limit, offset);

        return handleError(clientsApi.retrieveAllClients(officeId, searchText, status, limit, offset), "client retrieval");
    }

    public Observable<List<OfficeDTO>> retrieveAllOffices() {
        log.info("Retrieving all offices");

        return handleError(clientsApi.retrieveAllOffices(), "office retrieval");
    }

    public Observable<List<StaffData>> retrieveAllStaff() {
        log.info("Retrieving all staff");

        return handleError(clientsApi.retrieveAllStaff(), "staff retrieval");
    }

    public Observable<GetClientsClientIdAccountsResponse> retrieveClientAccounts(Long clientId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Retrieving accounts for client with ID: {}", clientId);

        return handleError(clientsApi.retrieveClientAccounts(clientId), "client accounts retrieval");
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

    public Observable<PostClientsClientIdResponse> withdrawClient(Long clientId, String command, @Valid ClientWithdrawRequestDTO withdrawRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("command", command);
            params.put("withdrawRequest", withdrawRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Applying withdraw command {} to client with ID: {}", command, clientId);

        return handleError(clientsApi.applyCommand(clientId.toString(), withdrawRequest.toMap(), command), "client withdrawal");
    }

    public Observable<PostClientsClientIdResponse> reactivateClient(Long clientId, String command, @Valid ClientReactivateRequestDTO reactivateRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("command", command);
            params.put("reactivateRequest", reactivateRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Applying reactivate command {} to client with ID: {}", command, clientId);

        return handleError(clientsApi.applyCommand(clientId.toString(), reactivateRequest.toMap(), command), "client reactivation");
    }

    public Observable<PostClientsClientIdResponse> undoRejectClient(Long clientId, String command, @Valid ClientUndoRejectRequestDTO undoRejectRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("command", command);
            params.put("undoRejectRequest", undoRejectRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Applying undo reject command {} to client with ID: {}", command, clientId);

        return handleError(clientsApi.applyCommand(clientId.toString(), undoRejectRequest.toMap(), command), "client undo reject");
    }

    public Observable<PostClientsClientIdResponse> undoWithdrawClient(Long clientId, String command, @Valid ClientUndoWithdrawRequestDTO undoWithdrawRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("command", command);
            params.put("undoWithdrawRequest", undoWithdrawRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Applying undo withdraw command {} to client with ID: {}", command, clientId);

        return handleError(clientsApi.applyCommand(clientId.toString(), undoWithdrawRequest.toMap(), command), "client undo withdraw");
    }

    public Observable<PostClientsClientIdResponse> assignStaff(Long clientId, String command, @Valid ClientAssignStaffRequestDTO assignStaffRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("command", command);
            params.put("assignStaffRequest", assignStaffRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Applying assign staff command {} to client with ID: {}", command, clientId);

        return handleError(clientsApi.applyCommand(clientId.toString(), assignStaffRequest.toMap(), command), "client staff assignment");
    }

    public Observable<PostClientsClientIdResponse> unassignStaff(Long clientId, String command, @Valid ClientUnassignStaffRequestDTO unassignStaffRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("command", command);
            params.put("unassignStaffRequest", unassignStaffRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Applying unassign staff command {} to client with ID: {}", command, clientId);

        return handleError(clientsApi.applyCommand(clientId.toString(), unassignStaffRequest.toMap(), command), "client staff unassignment");
    }

    public Observable<PostClientsClientIdResponse> updateDefaultSavingsAccount(Long clientId, String command, @Valid ClientUpdateSavingsRequestDTO updateSavingsRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("command", command);
            params.put("updateSavingsRequest", updateSavingsRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Applying update default savings account command {} to client with ID: {}", command, clientId);

        return handleError(clientsApi.applyCommand(clientId.toString(), updateSavingsRequest.toMap(), command), "client default savings account update");
    }

    public Observable<PostClientsClientIdResponse> proposeClientTransfer(Long clientId, @Valid ClientTransferRequestDTO transferRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("transferRequest", transferRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Proposing transfer for client with ID: {}", clientId);

        return handleError(
                clientsApi.retrieveTransferTemplate(clientId)
                        .flatMap(template -> {
                            log.info("Retrieved transfer template for client: {}", clientId);
                            return clientsApi.applyCommand(clientId.toString(), transferRequest.toMap(), "proposeTransfer");
                        }),
                "client transfer proposal"
        );
    }

    public Observable<PostClientsClientIdResponse> withdrawClientTransfer(Long clientId, String command, @Valid ClientWithdrawTransferRequestDTO withdrawTransferRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("command", command);
            params.put("withdrawTransferRequest", withdrawTransferRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Applying withdraw transfer command {} to client with ID: {}", command, clientId);

        return handleError(clientsApi.applyCommand(clientId.toString(), withdrawTransferRequest.toMap(), command), "client transfer withdrawal");
    }

    public Observable<PostClientsClientIdResponse> rejectClientTransfer(Long clientId, String command, @Valid ClientRejectTransferRequestDTO rejectTransferRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("command", command);
            params.put("rejectTransferRequest", rejectTransferRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Applying reject transfer command {} to client with ID: {}", command, clientId);

        return handleError(clientsApi.applyCommand(clientId.toString(), rejectTransferRequest.toMap(), command), "client transfer rejection");
    }

    public Observable<PostClientsClientIdResponse> acceptClientTransfer(Long clientId, String command, @Valid ClientAcceptTransferRequestDTO acceptTransferRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("command", command);
            params.put("acceptTransferRequest", acceptTransferRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Applying accept transfer command {} to client with ID: {}", command, clientId);

        return handleError(clientsApi.applyCommand(clientId.toString(), acceptTransferRequest.toMap(), command), "client transfer acceptance");
    }

    public Observable<PostClientsClientIdResponse> proposeAndAcceptClientTransfer(Long clientId, @Valid ClientTransferRequestDTO transferRequest, @Valid ClientAcceptTransferRequestDTO acceptRequest) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            params.put("transferRequest", transferRequest);
            params.put("acceptRequest", acceptRequest);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Proposing and accepting transfer for client with ID: {}", clientId);

        return handleError(
                clientsApi.retrieveTransferTemplate(clientId)
                        .flatMap(template -> proposeTransfer(clientId, transferRequest, acceptRequest)),
                "client transfer proposal and acceptance"
        );
    }

    private Observable<PostClientsClientIdResponse> proposeTransfer(Long clientId, ClientTransferRequestDTO transferRequest, ClientAcceptTransferRequestDTO acceptRequest) {
        return clientsApi.applyCommand(clientId.toString(), transferRequest.toMap(), "proposeTransfer")
                .flatMap(response -> acceptTransfer(clientId, transferRequest, acceptRequest));
    }

    private Observable<PostClientsClientIdResponse> acceptTransfer(Long clientId, ClientTransferRequestDTO transferRequest, ClientAcceptTransferRequestDTO acceptRequest) {
        Map<String, Object> combinedRequest = new HashMap<>(transferRequest.toMap());
        combinedRequest.putAll(acceptRequest.toMap());
        return clientsApi.applyCommand(clientId.toString(), combinedRequest, "acceptTransfer");
    }

    public Observable<DeleteClientsClientIdResponse> deleteClient(Long clientId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("clientId", clientId);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Deleting client with ID: {}", clientId);
        return handleError(clientsApi.deleteClient(clientId), "client deletion");
    }

    public Observable<PostClientsClientIdResponse> applyCommandByExternalId(String externalId, String command, Map<String, Object> request) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("externalId", externalId);
            params.put("command", command);
            params.put("request", request);
            requireParamsNonNull(params);
        } catch (NullPointerException e) {
            return Observable.error(e);
        }
        log.info("Applying command {} to client with external ID: {}", command, externalId);

        return handleError(clientsApi.applyCommandByExternalId(externalId, request, command), "client command by external ID");
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