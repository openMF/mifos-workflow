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
import org.mifos.workflow.dto.fineract.client.*;
import org.mifos.workflow.dto.fineract.code.CodeDataDTO;
import org.mifos.workflow.service.fineract.auth.FineractAuthService;
import org.mifos.workflow.dto.fineract.office.OfficeDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
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
    void createBasicClient_NullFields_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.createBasicClient(null, "Doe", "1234567890", 1L, DATE_FORMAT, LOCALE, LEGAL_FORM_ID)
                    .blockingFirst();
        });

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
    void activateClient_NullClientId_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.activateClient(null, DATE_FORMAT, LOCALE)
                    .blockingFirst();
        });

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
    void retrieveClient_NullClientId_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.retrieveClient(null)
                    .blockingFirst();
        });

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
    void updateClient_NullClientId_ThrowsNullPointerException() {
        // Arrange
        ClientUpdateRequestDTO updateRequest = ClientUpdateRequestDTO.builder()
                .firstName("Updated")
                .lastName("Name")
                .mobileNo("1234567890")
                .externalId("EXT123")
                .active(true)
                .officeId(1L)
                .legalFormId(1L)
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.updateClient(null, updateRequest)
                    .blockingFirst();
        });

        verify(clientsApi, never()).updateClient(anyLong(), anyMap());
    }

    @Test
    void updateClient_NullUpdateRequest_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.updateClient(123L, null)
                    .blockingFirst();
        });

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
    void transferClient_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientTransferRequestDTO transferRequest = ClientTransferRequestDTO.builder()
                .transferDate(LocalDate.of(2023, 1, 1))
                .destinationOfficeId(2L)
                .dateFormat(DATE_FORMAT)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.transferClient(null, "transfer", transferRequest)
                    .blockingFirst();
        });

        verify(clientsApi, never()).retrieveTransferTemplate(anyLong());
    }

    @Test
    void transferClient_Success() {
        // Arrange
        ClientTransferRequestDTO transferRequest = ClientTransferRequestDTO.builder()
                .transferDate(LocalDate.of(2023, 1, 1))
                .destinationOfficeId(2L)
                .dateFormat(DATE_FORMAT)
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
                map.get("transferDate").equals(LocalDate.of(2023, 1, 1).format(java.time.format.DateTimeFormatter.ofPattern(DATE_FORMAT))) &&
                        map.get("destinationOfficeId").equals(2L) &&
                        map.get("dateFormat").equals(DATE_FORMAT)
        ), eq("transfer"));
    }

    @Test
    void rejectClient_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientRejectRequestDTO rejectRequest = ClientRejectRequestDTO.builder()
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .rejectionDate(LocalDate.of(2023, 1, 1))
                .rejectionReasonId(1L)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.rejectClient(null, "reject", rejectRequest)
                    .blockingFirst();
        });

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
                map.get("rejectionDate").equals(LocalDate.of(2023, 1, 1).format(java.time.format.DateTimeFormatter.ofPattern(DATE_FORMAT))) &&
                        map.get("rejectionReasonId").equals(1L) &&
                        map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE)
        ), eq("reject"));
    }

    @Test
    void closeClient_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientCloseRequestDTO closeRequest = ClientCloseRequestDTO.builder()
                .closureDate(LocalDate.of(2023, 1, 1))
                .closureReasonId(1L)
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.closeClient(null, "close", closeRequest)
                    .blockingFirst();
        });

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
                map.get("closureDate").equals(LocalDate.of(2023, 1, 1).format(java.time.format.DateTimeFormatter.ofPattern(DATE_FORMAT))) &&
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

    @Test
    void withdrawClient_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientWithdrawRequestDTO withdrawRequest = ClientWithdrawRequestDTO.builder()
                .withdrawalDate(LocalDate.of(2023, 1, 1))
                .withdrawalReasonId(1L)
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.withdrawClient(null, "withdraw", withdrawRequest)
                    .blockingFirst();
        });

        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void reactivateClient_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientReactivateRequestDTO reactivateRequest = ClientReactivateRequestDTO.builder()
                .reactivationDate(LocalDate.of(2023, 1, 1))
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.reactivateClient(null, "reactivate", reactivateRequest)
                    .blockingFirst();
        });

        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void undoRejectClient_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientUndoRejectRequestDTO undoRejectRequest = ClientUndoRejectRequestDTO.builder()
                .reopenedDate(LocalDate.of(2023, 1, 1))
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.undoRejectClient(null, "undoRejection", undoRejectRequest)
                    .blockingFirst();
        });

        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void undoWithdrawClient_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientUndoWithdrawRequestDTO undoWithdrawRequest = ClientUndoWithdrawRequestDTO.builder()
                .reopenedDate(LocalDate.of(2023, 1, 1))
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.undoWithdrawClient(null, "undoWithdrawal", undoWithdrawRequest)
                    .blockingFirst();
        });

        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void assignStaff_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientAssignStaffRequestDTO assignStaffRequest = ClientAssignStaffRequestDTO.builder()
                .staffId(1L)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.assignStaff(null, "assignStaff", assignStaffRequest)
                    .blockingFirst();
        });

        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void unassignStaff_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientUnassignStaffRequestDTO unassignStaffRequest = ClientUnassignStaffRequestDTO.builder()
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.unassignStaff(null, "unassignStaff", unassignStaffRequest)
                    .blockingFirst();
        });

        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void updateDefaultSavingsAccount_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientUpdateSavingsRequestDTO updateSavingsRequest = ClientUpdateSavingsRequestDTO.builder()
                .savingsAccountId(1L)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.updateDefaultSavingsAccount(null, "updateSavingsAccount", updateSavingsRequest)
                    .blockingFirst();
        });

        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void proposeClientTransfer_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientTransferRequestDTO transferRequest = ClientTransferRequestDTO.builder()
                .transferDate(LocalDate.of(2023, 1, 1))
                .destinationOfficeId(2L)
                .dateFormat(DATE_FORMAT)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.proposeClientTransfer(null, transferRequest)
                    .blockingFirst();
        });

        verify(clientsApi, never()).retrieveTransferTemplate(anyLong());
    }

    @Test
    void withdrawClientTransfer_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientWithdrawTransferRequestDTO withdrawTransferRequest = ClientWithdrawTransferRequestDTO.builder()
                .withdrawalDate(LocalDate.of(2023, 1, 1))
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.withdrawClientTransfer(null, "withdrawTransfer", withdrawTransferRequest)
                    .blockingFirst();
        });

        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void rejectClientTransfer_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientRejectTransferRequestDTO rejectTransferRequest = ClientRejectTransferRequestDTO.builder()
                .rejectionDate(LocalDate.of(2023, 1, 1))
                .rejectionReasonId("1")
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.rejectClientTransfer(null, "rejectTransfer", rejectTransferRequest)
                    .blockingFirst();
        });

        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void acceptClientTransfer_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientAcceptTransferRequestDTO acceptTransferRequest = ClientAcceptTransferRequestDTO.builder()
                .transferDate(LocalDate.of(2023, 1, 1))
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.acceptClientTransfer(null, "acceptTransfer", acceptTransferRequest)
                    .blockingFirst();
        });

        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void proposeAndAcceptClientTransfer_NullParameters_ThrowsNullPointerException() {
        // Arrange
        ClientTransferRequestDTO transferRequest = ClientTransferRequestDTO.builder()
                .transferDate(LocalDate.of(2023, 1, 1))
                .destinationOfficeId(2L)
                .dateFormat(DATE_FORMAT)
                .build();
        ClientAcceptTransferRequestDTO acceptRequest = ClientAcceptTransferRequestDTO.builder()
                .transferDate(LocalDate.of(2023, 1, 1))
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.proposeAndAcceptClientTransfer(null, transferRequest, acceptRequest)
                    .blockingFirst();
        });

        verify(clientsApi, never()).retrieveTransferTemplate(anyLong());
    }

    @Test
    void deleteClient_NullClientId_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.deleteClient(null)
                    .blockingFirst();
        });

        verify(clientsApi, never()).deleteClient(anyLong());
    }

    @Test
    void applyCommandByExternalId_NullParameters_ThrowsNullPointerException() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("command", "activate");

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.applyCommandByExternalId(null, "activate", request)
                    .blockingFirst();
        });

        verify(clientsApi, never()).applyCommandByExternalId(anyString(), anyMap(), anyString());
    }

    @Test
    void retrieveAllStaff_Success() {
        // Arrange
        List<StaffData> staffList = new ArrayList<>();
        StaffData staff1 = mock(StaffData.class);
        lenient().when(staff1.getId()).thenReturn(1L);
        lenient().when(staff1.getDisplayName()).thenReturn("Staff 1");
        staffList.add(staff1);

        StaffData staff2 = mock(StaffData.class);
        lenient().when(staff2.getId()).thenReturn(2L);
        lenient().when(staff2.getDisplayName()).thenReturn("Staff 2");
        staffList.add(staff2);

        when(clientsApi.retrieveAllStaff()).thenReturn(Observable.just(staffList));

        // Act
        TestObserver<List<StaffData>> testObserver = clientService.retrieveAllStaff()
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(staffList);
        testObserver.assertComplete();

        verify(clientsApi).retrieveAllStaff();
    }

    @Test
    void retrieveClientAccounts_Success() {
        // Arrange
        GetClientsClientIdAccountsResponse response = mock(GetClientsClientIdAccountsResponse.class);
        when(clientsApi.retrieveClientAccounts(anyLong())).thenReturn(Observable.just(response));

        // Act
        TestObserver<GetClientsClientIdAccountsResponse> testObserver = clientService.retrieveClientAccounts(123L)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(response);
        testObserver.assertComplete();

        verify(clientsApi).retrieveClientAccounts(123L);
    }

    @Test
    void retrieveClientAccounts_NullClientId_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.retrieveClientAccounts(null)
                    .blockingFirst();
        });

        verify(clientsApi, never()).retrieveClientAccounts(anyLong());
    }

    @Test
    void retrieveAllClients_NullOfficeId_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientService.retrieveAllClients(null, "search", "active", 10, 0)
                    .blockingFirst();
        });

        verify(clientsApi, never()).retrieveAllClients(anyLong(), anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    void withdrawClient_Success() {
        // Arrange
        ClientWithdrawRequestDTO withdrawRequest = ClientWithdrawRequestDTO.builder()
                .withdrawalDate(LocalDate.of(2023, 1, 1))
                .withdrawalReasonId(1L)
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.withdrawClient(123L, "withdraw", withdrawRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommand(eq("123"), argThat(map ->
                map.get("withdrawalDate").equals(LocalDate.of(2023, 1, 1).format(DateTimeFormatter.ofPattern(DATE_FORMAT))) &&
                        map.get("withdrawalReasonId").equals(1L) &&
                        map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE)
        ), eq("withdraw"));
    }

    @Test
    void reactivateClient_Success() {
        // Arrange
        ClientReactivateRequestDTO reactivateRequest = ClientReactivateRequestDTO.builder()
                .reactivationDate(LocalDate.of(2023, 1, 1))
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.reactivateClient(123L, "reactivate", reactivateRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommand(eq("123"), argThat(map ->
                map.get("reactivationDate").equals(LocalDate.of(2023, 1, 1).format(DateTimeFormatter.ofPattern(DATE_FORMAT))) &&
                        map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE)
        ), eq("reactivate"));
    }

    @Test
    void undoRejectClient_Success() {
        // Arrange
        ClientUndoRejectRequestDTO undoRejectRequest = ClientUndoRejectRequestDTO.builder()
                .reopenedDate(LocalDate.of(2023, 1, 1))
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.undoRejectClient(123L, "undoRejection", undoRejectRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommand(eq("123"), argThat(map ->
                map.get("reopenedDate").equals(LocalDate.of(2023, 1, 1).format(DateTimeFormatter.ofPattern(DATE_FORMAT))) &&
                        map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE)
        ), eq("undoRejection"));
    }

    @Test
    void undoWithdrawClient_Success() {
        // Arrange
        ClientUndoWithdrawRequestDTO undoWithdrawRequest = ClientUndoWithdrawRequestDTO.builder()
                .reopenedDate(LocalDate.of(2023, 1, 1))
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.undoWithdrawClient(123L, "undoWithdrawal", undoWithdrawRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommand(eq("123"), argThat(map ->
                map.get("reopenedDate").equals(LocalDate.of(2023, 1, 1).format(DateTimeFormatter.ofPattern(DATE_FORMAT))) &&
                        map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE)
        ), eq("undoWithdrawal"));
    }

    @Test
    void assignStaff_Success() {
        // Arrange
        ClientAssignStaffRequestDTO assignStaffRequest = ClientAssignStaffRequestDTO.builder()
                .staffId(1L)
                .build();
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.assignStaff(123L, "assignStaff", assignStaffRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommand(eq("123"), argThat(map ->
                map.get("staffId").equals(1L)
        ), eq("assignStaff"));
    }

    @Test
    void unassignStaff_Success() {
        // Arrange
        ClientUnassignStaffRequestDTO unassignStaffRequest = ClientUnassignStaffRequestDTO.builder()
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.unassignStaff(123L, "unassignStaff", unassignStaffRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommand(eq("123"), argThat(map ->
                map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE)
        ), eq("unassignStaff"));
    }

    @Test
    void updateDefaultSavingsAccount_Success() {
        // Arrange
        ClientUpdateSavingsRequestDTO updateSavingsRequest = ClientUpdateSavingsRequestDTO.builder()
                .savingsAccountId(1L)
                .build();
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.updateDefaultSavingsAccount(123L, "updateSavingsAccount", updateSavingsRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommand(eq("123"), argThat(map ->
                map.get("savingsAccountId").equals(1L)
        ), eq("updateSavingsAccount"));
    }

    @Test
    void proposeClientTransfer_Success() {
        // Arrange
        ClientTransferRequestDTO transferRequest = ClientTransferRequestDTO.builder()
                .transferDate(LocalDate.of(2023, 1, 1))
                .destinationOfficeId(2L)
                .dateFormat(DATE_FORMAT)
                .build();
        GetClientTransferProposalDateResponse template = mock(GetClientTransferProposalDateResponse.class);
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.retrieveTransferTemplate(anyLong())).thenReturn(Observable.just(template));
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.proposeClientTransfer(123L, transferRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).retrieveTransferTemplate(123L);
        verify(clientsApi).applyCommand(eq("123"), argThat(map ->
                map.get("transferDate").equals(LocalDate.of(2023, 1, 1).format(DateTimeFormatter.ofPattern(DATE_FORMAT))) &&
                        map.get("destinationOfficeId").equals(2L) &&
                        map.get("dateFormat").equals(DATE_FORMAT)
        ), eq("proposeTransfer"));
    }

    @Test
    void withdrawClientTransfer_Success() {
        // Arrange
        ClientWithdrawTransferRequestDTO withdrawTransferRequest = ClientWithdrawTransferRequestDTO.builder()
                .withdrawalDate(LocalDate.of(2023, 1, 1))
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.withdrawClientTransfer(123L, "withdrawTransfer", withdrawTransferRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommand(eq("123"), argThat(map ->
                map.get("withdrawalDate").equals(LocalDate.of(2023, 1, 1).format(DateTimeFormatter.ofPattern(DATE_FORMAT))) &&
                        map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE)
        ), eq("withdrawTransfer"));
    }

    @Test
    void rejectClientTransfer_Success() {
        // Arrange
        ClientRejectTransferRequestDTO rejectTransferRequest = ClientRejectTransferRequestDTO.builder()
                .rejectionDate(LocalDate.of(2023, 1, 1))
                .rejectionReasonId("1")
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.rejectClientTransfer(123L, "rejectTransfer", rejectTransferRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommand(eq("123"), argThat(map ->
                map.get("rejectionDate").equals(LocalDate.of(2023, 1, 1).format(DateTimeFormatter.ofPattern(DATE_FORMAT))) &&
                        map.get("rejectionReasonId").equals("1") &&
                        map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE)
        ), eq("rejectTransfer"));
    }

    @Test
    void acceptClientTransfer_Success() {
        // Arrange
        ClientAcceptTransferRequestDTO acceptTransferRequest = ClientAcceptTransferRequestDTO.builder()
                .transferDate(LocalDate.of(2023, 1, 1))
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.acceptClientTransfer(123L, "acceptTransfer", acceptTransferRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommand(eq("123"), argThat(map ->
                map.get("transferDate").equals(LocalDate.of(2023, 1, 1).format(DateTimeFormatter.ofPattern(DATE_FORMAT))) &&
                        map.get("dateFormat").equals(DATE_FORMAT) &&
                        map.get("locale").equals(LOCALE)
        ), eq("acceptTransfer"));
    }

    @Test
    void proposeAndAcceptClientTransfer_Success() {
        // Arrange
        ClientTransferRequestDTO transferRequest = ClientTransferRequestDTO.builder()
                .transferDate(LocalDate.of(2023, 1, 1))
                .destinationOfficeId(2L)
                .dateFormat(DATE_FORMAT)
                .build();
        ClientAcceptTransferRequestDTO acceptRequest = ClientAcceptTransferRequestDTO.builder()
                .transferDate(LocalDate.of(2023, 1, 1))
                .dateFormat(DATE_FORMAT)
                .locale(LOCALE)
                .build();
        GetClientTransferProposalDateResponse template = mock(GetClientTransferProposalDateResponse.class);
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.retrieveTransferTemplate(anyLong())).thenReturn(Observable.just(template));
        when(clientsApi.applyCommand(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.proposeAndAcceptClientTransfer(123L, transferRequest, acceptRequest)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).retrieveTransferTemplate(123L);
        verify(clientsApi, times(2)).applyCommand(eq("123"), anyMap(), anyString());
    }

    @Test
    void deleteClient_Success() {
        // Arrange
        DeleteClientsClientIdResponse response = mock(DeleteClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.deleteClient(anyLong())).thenReturn(Observable.just(response));

        // Act
        TestObserver<DeleteClientsClientIdResponse> testObserver = clientService.deleteClient(123L)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).deleteClient(123L);
    }

    @Test
    void applyCommandByExternalId_Success() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("command", "activate");
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommandByExternalId(anyString(), anyMap(), anyString())).thenReturn(Observable.just(response));

        // Act
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.applyCommandByExternalId("EXT123", "activate", request)
                .test();

        // Assert
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommandByExternalId("EXT123", request, "activate");
    }
}