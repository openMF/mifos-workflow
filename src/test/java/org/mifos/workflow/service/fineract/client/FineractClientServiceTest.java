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
import org.mifos.workflow.dto.fineract.client.ClientCreateRequest;

import org.mifos.workflow.service.fineract.auth.FineractAuthService;
import org.mifos.workflow.dto.fineract.office.OfficeDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    private ClientCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = ClientCreateRequest.builder()
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
        PostClientsResponse response = mock(PostClientsResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.createClient(anyMap())).thenReturn(Observable.just(response));

        TestObserver<PostClientsResponse> testObserver = clientService.createClient(createRequest, DATE_FORMAT, LOCALE, ADDRESS_TYPE_ID)
                .test();

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
        TestObserver<PostClientsResponse> testObserver = clientService.createBasicClient(null, "Doe", "1234567890", 1L, DATE_FORMAT, LOCALE, LEGAL_FORM_ID)
                .test();
        
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalArgumentException.class);
        testObserver.assertError(throwable -> 
            throwable.getMessage().equals("Mandatory fields (firstname, lastname, officeId) cannot be null"));

        verify(clientsApi, never()).createClient(anyMap());
    }

    @Test
    void createBasicClient_Success() {
        PostClientsResponse response = mock(PostClientsResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.createClient(anyMap())).thenReturn(Observable.just(response));

        TestObserver<PostClientsResponse> testObserver = clientService.createBasicClient("John", "Doe", "1234567890", 1L, DATE_FORMAT, LOCALE, LEGAL_FORM_ID)
                .test();
        
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
        TestObserver<PostClientsClientIdResponse> testObserver = clientService.activateClient(null, DATE_FORMAT, LOCALE)
                .test();

        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(error ->
                error instanceof IllegalArgumentException &&
                        error.getMessage().equals("Client ID or activation date cannot be null"));

        verify(clientsApi, never()).activateClient(anyLong(), anyString(), anyMap());
    }

    @Test
    void activateClient_Success() {
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.activateClient(anyLong(), eq("activate"), anyMap()))
                .thenReturn(Observable.just(response));

        TestObserver<PostClientsClientIdResponse> testObserver = clientService.activateClient(123L, DATE_FORMAT, LOCALE)
                .test();

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
        LocalDate activationDate = LocalDate.of(2023, 1, 1);
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.activateClient(anyLong(), eq("activate"), anyMap()))
                .thenReturn(Observable.just(response));

        TestObserver<PostClientsClientIdResponse> testObserver = clientService.activateClient(123L, activationDate, DATE_FORMAT, LOCALE)
                .test();

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
        TestObserver<GetClientsClientIdResponse> testObserver = clientService.retrieveClient(null)
                .test();
        
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalArgumentException.class);
        testObserver.assertError(throwable -> 
            throwable.getMessage().equals("Client ID cannot be null"));

        verify(clientsApi, never()).retrieveClient(anyLong());
    }

    @Test
    void retrieveClient_Success() {
        GetClientsClientIdResponse response = mock(GetClientsClientIdResponse.class);
        when(response.getId()).thenReturn(123L);
        when(clientsApi.retrieveClient(anyLong()))
                .thenReturn(Observable.just(response));

        TestObserver<GetClientsClientIdResponse> testObserver = clientService.retrieveClient(123L)
                .test();
        
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).retrieveClient(123L);
    }

    @Test
    void updateClientWithMap_NullClientId_ReturnsError() {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("firstname", "Updated");

        TestObserver<PutClientsClientIdResponse> testObserver = clientService.updateClientWithMap(null, updateData)
                .test();
        
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalArgumentException.class);
        testObserver.assertError(throwable -> 
            throwable.getMessage().equals("Client ID or update data cannot be null"));

        verify(clientsApi, never()).updateClient(anyLong(), anyMap());
    }

    @Test
    void updateClientWithMap_NullUpdateData_ReturnsError() {
        TestObserver<PutClientsClientIdResponse> testObserver = clientService.updateClientWithMap(123L, null)
                .test();
        
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalArgumentException.class);
        testObserver.assertError(throwable -> 
            throwable.getMessage().equals("Client ID or update data cannot be null"));

        verify(clientsApi, never()).updateClient(anyLong(), anyMap());
    }

    @Test
    void updateClientWithMap_Success() {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("firstname", "Updated");
        PutClientsClientIdResponse response = mock(PutClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.updateClient(anyLong(), anyMap()))
                .thenReturn(Observable.just(response));

        TestObserver<PutClientsClientIdResponse> testObserver = clientService.updateClientWithMap(123L, updateData)
                .test();
        
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).updateClient(eq(123L), eq(updateData));
    }

    @Test
    void transferClient_NullParameters_ReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("destinationOfficeId", 2L);
        request.put("transferDate", LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));

        TestObserver<PostClientsClientIdResponse> testObserver = clientService.transferClient(null, "proposeTransfer", request)
                .test();

        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(error ->
                error instanceof IllegalArgumentException &&
                        error.getMessage().equals("Client ID, command, or request cannot be null"));

        verify(clientsApi, never()).retrieveTransferTemplate(anyLong());
        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void transferClient_Success() {
        Map<String, Object> request = new HashMap<>();
        request.put("destinationOfficeId", 2L);
        request.put("transferDate", LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));

        GetClientTransferProposalDateResponse templateResponse = mock(GetClientTransferProposalDateResponse.class);
        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);

        when(clientsApi.retrieveTransferTemplate(123L))
                .thenReturn(Observable.just(templateResponse));
        when(clientsApi.applyCommand(
                eq("123"),
                argThat(map ->
                        map.containsKey("proposedTransferDate") &&
                                map.get("destinationOfficeId").equals(2L)
                ),
                eq("proposeTransfer")))
                .thenReturn(Observable.just(response));

        TestObserver<PostClientsClientIdResponse> testObserver = clientService.transferClient(123L, "proposeTransfer", request)
                .test();

        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).retrieveTransferTemplate(123L);
        verify(clientsApi).applyCommand(
                eq("123"),
                argThat(map ->
                        map.containsKey("proposedTransferDate") &&
                                map.get("destinationOfficeId").equals(2L)
                ),
                eq("proposeTransfer"));
    }

    @Test
    void rejectClient_NullParameters_ReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("rejectionDate", LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        request.put("rejectionReasonId", 1L);

        TestObserver<PostClientsClientIdResponse> testObserver = clientService.rejectClient(null, "reject", request, DATE_FORMAT, LOCALE)
                .test();
        
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalArgumentException.class);
        testObserver.assertError(throwable -> 
            throwable.getMessage().equals("Client ID, command, or request cannot be null"));

        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void rejectClient_Success() {
        Map<String, Object> request = new HashMap<>();
        request.put("rejectionDate", LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        request.put("rejectionReasonId", 1L);

        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommand(anyString(), anyMap(), eq("reject")))
                .thenReturn(Observable.just(response));

        TestObserver<PostClientsClientIdResponse> testObserver = clientService.rejectClient(123L, "reject", request, DATE_FORMAT, LOCALE)
                .test();
        
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommand(
            eq("123"), 
            argThat(map -> 
                map.get("rejectionDate").equals(request.get("rejectionDate")) &&
                map.get("rejectionReasonId").equals(request.get("rejectionReasonId")) &&
                map.get("dateFormat").equals(DATE_FORMAT) &&
                map.get("locale").equals(LOCALE)
            ), 
            eq("reject"));
    }

    @Test
    void closeClient_NullParameters_ReturnsError() {
        Map<String, Object> request = new HashMap<>();
        request.put("closureDate", LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        request.put("closureReasonId", 1L);

        TestObserver<PostClientsClientIdResponse> testObserver = clientService.closeClient(null, "close", request, DATE_FORMAT, LOCALE)
                .test();
        
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertNoValues();
        testObserver.assertError(IllegalArgumentException.class);
        testObserver.assertError(throwable -> 
            throwable.getMessage().equals("Client ID, command, or request cannot be null"));

        verify(clientsApi, never()).applyCommand(anyString(), anyMap(), anyString());
    }

    @Test
    void closeClient_Success() {
        Map<String, Object> request = new HashMap<>();
        request.put("closureDate", LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        request.put("closureReasonId", 1L);

        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getClientId()).thenReturn(123L);
        when(clientsApi.applyCommand(anyString(), anyMap(), eq("close")))
                .thenReturn(Observable.just(response));

        TestObserver<PostClientsClientIdResponse> testObserver = clientService.closeClient(123L, "close", request, DATE_FORMAT, LOCALE)
                .test();
        
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(r -> r.getClientId().equals(123L));
        testObserver.assertComplete();

        verify(clientsApi).applyCommand(
            eq("123"), 
            argThat(map -> 
                map.get("closureDate").equals(request.get("closureDate")) &&
                map.get("closureReasonId").equals(request.get("closureReasonId")) &&
                map.get("dateFormat").equals(DATE_FORMAT) &&
                map.get("locale").equals(LOCALE)
            ), 
            eq("close"));
    }

    @Test
    void retrieveAllClients_Success() {
        GetClientsResponse response = mock(GetClientsResponse.class);
        when(clientsApi.retrieveAllClients(anyLong(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(Observable.just(response));

        TestObserver<GetClientsResponse> testObserver = clientService.retrieveAllClients(1L, "search", "active", 10, 0)
                .test();
        
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(response);
        testObserver.assertComplete();

        verify(clientsApi).retrieveAllClients(1L, "search", "active", 10, 0);
    }

    @Test
    void retrieveAllOffices_Success() {
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

        TestObserver<List<OfficeDTO>> testObserver = clientService.retrieveAllOffices()
                .test();
        
        testObserver.awaitDone(5, TimeUnit.SECONDS);
        testObserver.assertValue(offices);
        testObserver.assertComplete();

        verify(clientsApi).retrieveAllOffices();
    }
}