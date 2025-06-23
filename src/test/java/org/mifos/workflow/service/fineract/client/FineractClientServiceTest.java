package org.mifos.workflow.service.fineract.client;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mifos.fineract.client.models.*;
import org.mifos.workflow.api.client.ClientsApi;
import org.mifos.workflow.dto.fineract.client.ClientCreateRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientUpdateRequestDTO;
import org.mifos.workflow.dto.fineract.code.CodeDataDTO;
import org.mifos.workflow.dto.fineract.client.ClientTransferRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientRejectRequestDTO;
import org.mifos.workflow.dto.fineract.client.ClientCloseRequestDTO;

import org.mifos.workflow.service.fineract.auth.FineractAuthService;
import org.mifos.workflow.dto.fineract.office.OfficeDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FineractClientServiceTest {

    @Mock
    private ClientsApi clientsApi;

    @Mock
    private FineractAuthService authService;

    @InjectMocks
    private FineractClientService clientService;

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String LOCALE = "en";
    private static final Long ADDRESS_TYPE_ID = 1L;
    private static final Long LEGAL_FORM_ID = 1L;

    private ClientCreateRequestDTO createRequest;

    @BeforeEach
    void setUp() {
        createRequest = ClientCreateRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .mobileNo("1234567890")
                .officeId(1L)
                .active(false)
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .legalFormId(LEGAL_FORM_ID)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .externalId("EXT123")
                .addressLine1("123 Main St")
                .city("New York")
                .stateProvinceId("1")
                .countryId("1")
                .postalCode("10001")
                .build();
    }

    @Test
    void createClient_Success() {
        // Arrange
        PostClientsResponse response = mock(PostClientsResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.createClient(anyMap())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsResponse> testObserver = clientService.createClient(createRequest, DATE_FORMAT, LOCALE, ADDRESS_TYPE_ID)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).createClient(argThat(map ->
                map.get("firstname").equals("John") &&
                        map.get("lastname").equals("Doe") &&
                        map.get("officeId").equals(1L) &&
                        map.get("legalFormId").equals(LEGAL_FORM_ID) &&
                        map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE)
        ));
    }

    @Test
    void createBasicClient_NullFields_ReturnsError() {

        // Act
        TestObserver<PostClientsResponse> testObserver = clientService.createBasicClient(null, "Doe", "1234567890", 1L, DATE_FORMAT, LOCALE, LEGAL_FORM_ID)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalArgumentException.class);
        testObserver.assertError(throwable ->
                throwable.getMessage().equals("Mandatory fields (firstname, lastname, officeId) cannot be null"));

        verify(clientsApi, never()).createClient(anyMap());
    }

    @Test
    void createBasicClient_Success() {
        // Arrange
        PostClientsResponse response = mock(PostClientsResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.createClient(anyMap())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsResponse> testObserver = clientService.createBasicClient("John", "Doe", "1234567890", 1L, DATE_FORMAT, LOCALE, LEGAL_FORM_ID)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).createClient(argThat(map ->
                map.get("firstname").equals("John") &&
                        map.get("lastname").equals("Doe") &&
                        map.get("mobileNo").equals("1234567890") &&
                        map.get("officeId").equals(1L) &&
                        map.get("legalFormId").equals(LEGAL_FORM_ID) &&
                        map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE)
        ));
    }

    @Test
    void activateClient_NullClientId_ReturnsError() {
        // Arrange
        // No specific arrangement needed for this test

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.activateClient(null, DATE_FORMAT, LOCALE)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(error ->
                error instanceof IllegalArgumentException &&
                        error.getMessage().equals("Client ID or activation date cannot be null"));

        verify(clientsApi, never()).activateClient(anyLong(), anyString(), anyMap());
    }

    @Test
    void activateClient_Success() {
        // Arrange
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.activateClient(anyLong(), eq("activate"), anyMap()))
                .thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.activateClient(123L, DATE_FORMAT, LOCALE)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).activateClient(eq(123L), eq("activate"), argThat(map ->
                map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE) &&
                        map.containsKey("activationDate")
        ));
    }

    @Test
    void activateClient_WithDate_Success() {
        // Arrange
        LocalDate activationDate = LocalDate.of(2023, 1, 1);
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.activateClient(anyLong(), eq("activate"), anyMap()))
                .thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.activateClient(123L, activationDate, DATE_FORMAT, LOCALE)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).activateClient(eq(123L), eq("activate"), argThat(map ->
                map.get("activationDate").equals(activationDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT))) &&
                        map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE)
        ));
    }

    @Test
    void retrieveClient_NullClientId_ReturnsError() {
        // Arrange
        // No specific arrangement needed for this test

        // Act
        TestObserver<GetClientsClientIdResponse> testObserver = clientService.retrieveClient(null)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalArgumentException.class);
        testObserver.assertError(throwable ->
                throwable.getMessage().equals("Client ID cannot be null"));

        verify(clientsApi, never()).retrieveClient(anyLong());
    }

    @Test
    void retrieveClient_Success() {
        // Arrange
        GetClientsClientIdResponse response = mock(GetClientsClientIdResponse.class);
        when(response.getId()).thenReturn(123L);
        when(clientsApi.retrieveClient(anyLong()))
                .thenReturn(Observable.just(response));

        // Act
        TestObserver<GetClientsClientIdResponse> testObserver = clientService.retrieveClient(123L)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).retrieveClient(123L);
    }

    @Test
    void updateClient_NullClientId_ReturnsError() {
        // Arrange
        ClientUpdateRequestDTO updateRequest = ClientUpdateRequestDTO.builder()
                .firstName("Updated")
                .lastName("Name")
                .build();

        // Act
        TestObserver<PutClientsClientIdResponse> testObserver = clientService.updateClient(null, updateRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalArgumentException.class);
        testObserver.assertError(throwable ->
                throwable.getMessage().equals("Client ID or update request cannot be null"));

        verify(clientsApi, never()).updateClient(anyLong(), anyMap());
    }

    @Test
    void updateClient_NullUpdateRequest_ReturnsError() {
        // Arrange
        // No specific arrangement needed for this test

        // Act
        TestObserver<PutClientsClientIdResponse> testObserver = clientService.updateClient(123L, null)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalArgumentException.class);
        testObserver.assertError(throwable ->
                throwable.getMessage().equals("Client ID or update request cannot be null"));

        verify(clientsApi, never()).updateClient(anyLong(), anyMap());
    }

    @Test
    void updateClient_Success() {
        // Arrange
        ClientUpdateRequestDTO updateRequest = ClientUpdateRequestDTO.builder()
                .firstName("Updated")
                .lastName("Name")
                .mobileNo("9876543210")
                .build();

        PutClientsClientIdResponse response = mock(PutClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.updateClient(anyLong(), anyMap())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PutClientsClientIdResponse> testObserver = clientService.updateClient(123L, updateRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).updateClient(eq(123L), argThat(map ->
                map.get("firstname").equals("Updated") &&
                        map.get("lastname").equals("Name") &&
                        map.get("mobileNo").equals("9876543210")
        ));
    }

    @Test
    void transferClient_NullParameters_ReturnsError() {
        // Arrange
        ClientTransferRequestDTO transferRequest = null;

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.transferClient(null, "transfer", transferRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalArgumentException.class);
        testObserver.assertError(throwable ->
                throwable.getMessage().equals("Client ID, command, or transfer request cannot be null"));

        verify(clientsApi, never()).retrieveTransferTemplate(anyLong());
    }

    @Test
    void transferClient_Success() {
        // Arrange
        ClientTransferRequestDTO transferRequest = ClientTransferRequestDTO.builder()
                .transferDate(LocalDate.of(2023, 1, 1))
                .destinationOfficeId(2L)
                .build();
        GetClientTransferProposalDateResponse template = mock(GetClientTransferProposalDateResponse.class);
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.retrieveTransferTemplate(anyLong())).thenReturn(Observable.just(template));
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.transferClient(123L, "transfer", transferRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).retrieveTransferTemplate(123L);
        verify(clientsApi).applyCommand(eq("123"), argThat(map ->
                map.get("transferDate").equals(LocalDate.of(2023, 1, 1)) &&
                        map.get("destinationOfficeId").equals(2L)
        ), eq("transfer"));
    }

    @Test
    void rejectClient_NullParameters_ReturnsError() {
        // Arrange
        ClientRejectRequestDTO rejectRequest = null;

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.rejectClient(null, "reject", rejectRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalArgumentException.class);
        testObserver.assertError(throwable ->
                throwable.getMessage().equals("Client ID, command, or reject request cannot be null"));

        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void rejectClient_Success() {
        // Arrange
        ClientRejectRequestDTO rejectRequest = ClientRejectRequestDTO.builder()
                .rejectionDate(LocalDate.of(2023, 1, 1))
                .rejectionReasonId(1L)
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.rejectClient(123L, "reject", rejectRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommand(eq("123"), argThat(map ->
                map.get("rejectionDate").equals(LocalDate.of(2023, 1, 1)) &&
                        map.get("rejectionReasonId").equals(1L) &&
                        map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE)
        ), eq("reject"));
    }

    @Test
    void closeClient_NullParameters_ReturnsError() {
        // Arrange
        ClientCloseRequestDTO closeRequest = null;

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.closeClient(null, "close", closeRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalArgumentException.class);
        testObserver.assertError(throwable ->
                throwable.getMessage().equals("Client ID, command, or close request cannot be null"));

        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void closeClient_Success() {
        // Arrange
        ClientCloseRequestDTO closeRequest = ClientCloseRequestDTO.builder()
                .closureDate(LocalDate.of(2023, 1, 1))
                .closureReasonId(1L)
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.closeClient(123L, "close", closeRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommand(eq("123"), argThat(map ->
                map.get("closureDate").equals(LocalDate.of(2023, 1, 1)) &&
                        map.get("closureReasonId").equals(1L) &&
                        map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE)
        ), eq("close"));
    }

    @Test
    void retrieveAllClients_Success() {
        // Arrange
        GetClientsResponse response = mock(GetClientsResponse.class);
        when(clientsApi.retrieveAllClients(anyLong(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(Observable.just(response));

        // Act
        TestObserver<GetClientsResponse> testObserver = clientService.retrieveAllClients(1L, "search", "active", 10, 0)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(response);
        testObserver.assertComplete();

        verify(clientsApi).retrieveAllClients(1L, "search", "active", 10, 0);
    }

    @Test
    void retrieveAllOffices_Success() {
        // Arrange
        List<OfficeDTO> offices = new ArrayList<>();
        OfficeDTO office1 = new OfficeDTO();
        office1.setId(1L);
        office1.setName("Office 1");
        offices.add(office1);

        OfficeDTO office2 = new OfficeDTO();
        office2.setId(2L);
        office2.setName("Office 2");
        offices.add(office2);

        when(clientsApi.retrieveAllOffices())
                .thenReturn(Observable.just(offices));

        // Act
        TestObserver<List<OfficeDTO>> testObserver = clientService.retrieveAllOffices()
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(offices);
        testObserver.assertComplete();

        verify(clientsApi).retrieveAllOffices();
    }

    @Test
    void retrieveClientRejectionReasons_Success() {
        // Arrange
        // Mock codes response
        List<CodeDataDTO> codes = new ArrayList<>();
        CodeDataDTO rejectionCode = new CodeDataDTO();
        rejectionCode.setId(1L);
        rejectionCode.setName("ClientRejectReason");
        codes.add(rejectionCode);

        // Mock rejection reasons response
        List<CodeValueData> rejectionReasons = new ArrayList<>();
        CodeValueData reason1 = mock(CodeValueData.class);
        when(reason1.getId()).thenReturn(1L);
        when(reason1.getName()).thenReturn("Invalid Documents");
        rejectionReasons.add(reason1);

        CodeValueData reason2 = mock(CodeValueData.class);
        when(reason2.getId()).thenReturn(2L);
        when(reason2.getName()).thenReturn("Incomplete Information");
        rejectionReasons.add(reason2);

        when(clientsApi.retrieveCodes()).thenReturn(Observable.just(codes));
        when(clientsApi.retrieveAllCodeValues(1L)).thenReturn(Observable.just(rejectionReasons));

        // Act
        TestObserver<List<CodeValueData>> testObserver = clientService.retrieveClientRejectionReasons()
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(rejectionReasons);
        testObserver.assertComplete();

        verify(clientsApi).retrieveCodes();
        verify(clientsApi).retrieveAllCodeValues(1L);
    }

    @Test
    void retrieveClientRejectionReasons_CodeNotFound_ReturnsError() {
        // Arrange
        // Mock codes response without ClientRejectReason
        List<CodeDataDTO> codes = new ArrayList<>();
        CodeDataDTO otherCode = new CodeDataDTO();
        otherCode.setId(2L);
        otherCode.setName("OtherCode");
        codes.add(otherCode);

        when(clientsApi.retrieveCodes()).thenReturn(Observable.just(codes));

        // Act
        TestObserver<List<CodeValueData>> testObserver = clientService.retrieveClientRejectionReasons()
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalStateException.class);
        testObserver.assertError(throwable ->
                throwable.getMessage().equals("Client rejection reason code not found in the system"));

        verify(clientsApi).retrieveCodes();
        verify(clientsApi, never()).retrieveAllCodeValues(anyLong());
    }

    @Test
    void retrieveClientRejectionReasons_NoReasonsFound_ReturnsError() {
        // Arrange
        // Mock codes response
        List<CodeDataDTO> codes = new ArrayList<>();
        CodeDataDTO rejectionCode = new CodeDataDTO();
        rejectionCode.setId(1L);
        rejectionCode.setName("ClientRejectReason");
        codes.add(rejectionCode);

        // Mock empty rejection reasons response
        List<CodeValueData> emptyReasons = new ArrayList<>();

        when(clientsApi.retrieveCodes()).thenReturn(Observable.just(codes));
        when(clientsApi.retrieveAllCodeValues(1L)).thenReturn(Observable.just(emptyReasons));

        // Act
        TestObserver<List<CodeValueData>> testObserver = clientService.retrieveClientRejectionReasons()
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalStateException.class);
        testObserver.assertError(throwable ->
                throwable.getMessage().equals("No rejection reasons found in the system. Please add rejection reasons first."));

        verify(clientsApi).retrieveCodes();
        verify(clientsApi).retrieveAllCodeValues(1L);
    }

    @Test
    void createRejectionReason_Success() {
        // Arrange
        // Mock codes response
        List<CodeDataDTO> codes = new ArrayList<>();
        CodeDataDTO rejectionCode = new CodeDataDTO();
        rejectionCode.setId(1L);
        rejectionCode.setName("ClientRejectReason");
        codes.add(rejectionCode);

        // Mock created rejection reason response
        CodeValueData createdReason = mock(CodeValueData.class);

        when(clientsApi.retrieveCodes()).thenReturn(Observable.just(codes));
        when(clientsApi.createCodeValue(eq(1L), anyMap())).thenReturn(Observable.just(createdReason));

        // Act
        CodeValueData result = clientService.createRejectionReason("New Rejection Reason", "Description")
                .blockingFirst();

        // Assert
        assertNotNull(result);
        assertEquals(createdReason, result);

        verify(clientsApi).retrieveCodes();
        verify(clientsApi).createCodeValue(eq(1L), argThat(map ->
                map.get("name").equals("New Rejection Reason") &&
                        map.get("description").equals("Description") &&
                        map.get("position").equals(1) &&
                        map.get("isActive").equals(true)
        ));
    }

    @Test
    void createRejectionReason_CodeNotFound_ReturnsError() {
        // Arrange
        // Mock codes response without ClientRejectReason
        List<CodeDataDTO> codes = new ArrayList<>();
        CodeDataDTO otherCode = new CodeDataDTO();
        otherCode.setId(2L);
        otherCode.setName("OtherCode");
        codes.add(otherCode);

        when(clientsApi.retrieveCodes()).thenReturn(Observable.just(codes));

        // Act
        TestObserver<CodeValueData> testObserver = clientService.createRejectionReason("New Reason", "Description")
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalStateException.class);
        testObserver.assertError(throwable ->
                throwable.getMessage().equals("Client rejection reason code not found in the system"));

        verify(clientsApi).retrieveCodes();
        verify(clientsApi, never()).createCodeValue(anyLong(), anyMap());
    }

    @Test
    void retrieveClientClosureReasons_Success() {
        // Arrange
        // Mock codes response
        List<CodeDataDTO> codes = new ArrayList<>();
        CodeDataDTO closureCode = new CodeDataDTO();
        closureCode.setId(2L);
        closureCode.setName("ClientClosureReason");
        codes.add(closureCode);

        // Mock closure reasons response
        List<CodeValueData> closureReasons = new ArrayList<>();
        CodeValueData reason1 = mock(CodeValueData.class);
        when(reason1.getId()).thenReturn(1L);
        when(reason1.getName()).thenReturn("Client Requested");
        closureReasons.add(reason1);

        CodeValueData reason2 = mock(CodeValueData.class);
        when(reason2.getId()).thenReturn(2L);
        when(reason2.getName()).thenReturn("Account Inactive");
        closureReasons.add(reason2);

        when(clientsApi.retrieveCodes()).thenReturn(Observable.just(codes));
        when(clientsApi.retrieveAllCodeValues(2L)).thenReturn(Observable.just(closureReasons));

        // Act
        TestObserver<List<CodeValueData>> testObserver = clientService.retrieveClientClosureReasons()
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(closureReasons);
        testObserver.assertComplete();

        verify(clientsApi).retrieveCodes();
        verify(clientsApi).retrieveAllCodeValues(2L);
    }

    @Test
    void retrieveClientClosureReasons_CodeNotFound_ReturnsError() {
        // Arrange
        // Mock codes response without ClientClosureReason
        List<CodeDataDTO> codes = new ArrayList<>();
        CodeDataDTO otherCode = new CodeDataDTO();
        otherCode.setId(1L);
        otherCode.setName("OtherCode");
        codes.add(otherCode);

        when(clientsApi.retrieveCodes()).thenReturn(Observable.just(codes));

        // Act
        TestObserver<List<CodeValueData>> testObserver = clientService.retrieveClientClosureReasons()
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalStateException.class);
        testObserver.assertError(throwable ->
                throwable.getMessage().equals("Client closure reason code not found in the system"));

        verify(clientsApi).retrieveCodes();
        verify(clientsApi, never()).retrieveAllCodeValues(anyLong());
    }

    @Test
    void retrieveClientClosureReasons_NoReasonsFound_ReturnsError() {
        // Arrange
        // Mock codes response
        List<CodeDataDTO> codes = new ArrayList<>();
        CodeDataDTO closureCode = new CodeDataDTO();
        closureCode.setId(2L);
        closureCode.setName("ClientClosureReason");
        codes.add(closureCode);

        // Mock empty closure reasons response
        List<CodeValueData> emptyReasons = new ArrayList<>();

        when(clientsApi.retrieveCodes()).thenReturn(Observable.just(codes));
        when(clientsApi.retrieveAllCodeValues(2L)).thenReturn(Observable.just(emptyReasons));

        // Act
        TestObserver<List<CodeValueData>> testObserver = clientService.retrieveClientClosureReasons()
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalStateException.class);
        testObserver.assertError(throwable ->
                throwable.getMessage().equals("No closure reasons found in the system. Please add closure reasons first."));

        verify(clientsApi).retrieveCodes();
        verify(clientsApi).retrieveAllCodeValues(2L);
    }

    @Test
    void createClosureReason_Success() {
        // Arrange
        // Mock codes response
        List<CodeDataDTO> codes = new ArrayList<>();
        CodeDataDTO closureCode = new CodeDataDTO();
        closureCode.setId(2L);
        closureCode.setName("ClientClosureReason");
        codes.add(closureCode);

        // Mock created closure reason response
        CodeValueData createdReason = mock(CodeValueData.class);

        when(clientsApi.retrieveCodes()).thenReturn(Observable.just(codes));
        when(clientsApi.createCodeValue(eq(2L), anyMap())).thenReturn(Observable.just(createdReason));

        // Act
        CodeValueData result = clientService.createClosureReason("New Closure Reason", "Description")
                .blockingFirst();

        // Assert
        assertNotNull(result);
        assertEquals(createdReason, result);

        verify(clientsApi).retrieveCodes();
        verify(clientsApi).createCodeValue(eq(2L), argThat(map ->
                map.get("name").equals("New Closure Reason") &&
                        map.get("description").equals("Description") &&
                        map.get("position").equals(1) &&
                        map.get("isActive").equals(true)
        ));
    }

    @Test
    void createClosureReason_CodeNotFound_ReturnsError() {
        // Arrange
        // Mock codes response without ClientClosureReason
        List<CodeDataDTO> codes = new ArrayList<>();
        CodeDataDTO otherCode = new CodeDataDTO();
        otherCode.setId(1L);
        otherCode.setName("OtherCode");
        codes.add(otherCode);

        when(clientsApi.retrieveCodes()).thenReturn(Observable.just(codes));

        // Act
        TestObserver<CodeValueData> testObserver = clientService.createClosureReason("New Reason", "Description")
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalStateException.class);
        testObserver.assertError(throwable ->
                throwable.getMessage().equals("Client closure reason code not found in the system"));

        verify(clientsApi).retrieveCodes();
        verify(clientsApi, never()).createCodeValue(anyLong(), anyMap());
    }
}