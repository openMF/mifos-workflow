package org.mifos.workflow.service.fineract.loan;

import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.fineract.client.models.*;
import org.mifos.workflow.api.loan.LoansApi;
import org.mifos.workflow.config.FineractApiConfig;
import org.mifos.workflow.exception.FineractApiException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.HttpException;
import retrofit2.Response;
import okhttp3.MediaType;
import okhttp3.ResponseBody;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FineractLoanServiceTest {

    @Mock
    private LoansApi loansApi;

    @Mock
    private FineractApiConfig fineractApiConfig;

    @InjectMocks
    private FineractLoanService fineractLoanService;

    private Map<String, Object> validLoanRequest;
    private GetLoansLoanIdResponse mockLoanResponse;
    private PostLoansResponse mockCreateResponse;
    private PostLoansLoanIdResponse mockStateTransitionResponse;

    @BeforeEach
    void setUp() {
        validLoanRequest = new HashMap<>();
        validLoanRequest.put("clientId", 1L);
        validLoanRequest.put("productId", 1L);
        validLoanRequest.put("principal", 10000.0);
        validLoanRequest.put("loanTermFrequency", 12);
        validLoanRequest.put("loanTermFrequencyType", 2);
        validLoanRequest.put("loanType", "individual");
        validLoanRequest.put("loanPurposeId", 1);
        validLoanRequest.put("interestRatePerPeriod", 10.0);
        validLoanRequest.put("interestRateFrequencyType", 2);
        validLoanRequest.put("amortizationType", 1);
        validLoanRequest.put("interestCalculationPeriodType", 1);
        validLoanRequest.put("transactionProcessingStrategyId", 1);
        validLoanRequest.put("loanDate", "2024-01-01");
        validLoanRequest.put("submittedOnDate", "2024-01-01");
        validLoanRequest.put("externalId", "EXT-001");

        mockLoanResponse = new GetLoansLoanIdResponse();
        mockCreateResponse = new PostLoansResponse();
        mockStateTransitionResponse = new PostLoansLoanIdResponse();
    }

    @Test
    void createLoan_Success() {
        // Given
        String command = "submit";
        when(loansApi.calculateLoanScheduleOrSubmitLoanApplication(validLoanRequest, command))
            .thenReturn(Observable.just(mockCreateResponse));

        // When
        PostLoansResponse response = fineractLoanService.createLoan(validLoanRequest, command);

        // Then
        assertNotNull(response);
        verify(loansApi).calculateLoanScheduleOrSubmitLoanApplication(validLoanRequest, command);
    }

    @Test
    void createLoan_WithCalculateCommand_Success() {
        // Given
        String command = "calculate";
        when(loansApi.calculateLoanScheduleOrSubmitLoanApplication(validLoanRequest, command))
            .thenReturn(Observable.just(mockCreateResponse));

        // When
        PostLoansResponse response = fineractLoanService.createLoan(validLoanRequest, command);

        // Then
        assertNotNull(response);
        verify(loansApi).calculateLoanScheduleOrSubmitLoanApplication(validLoanRequest, command);
    }

    @Test
    void getLoan_Success() {
        // Given
        Long loanId = 1L;
        Boolean staffInSelectedOfficeOnly = false;
        String associations = "all";
        String exclude = "none";
        String fields = "id,accountNo";

        when(loansApi.retrieveLoan(loanId, staffInSelectedOfficeOnly, associations, exclude, fields))
            .thenReturn(Observable.just(mockLoanResponse));

        // When
        GetLoansLoanIdResponse response = fineractLoanService.getLoan(loanId, staffInSelectedOfficeOnly, associations, exclude, fields);

        // Then
        assertNotNull(response);
        verify(loansApi).retrieveLoan(loanId, staffInSelectedOfficeOnly, associations, exclude, fields);
    }

    @Test
    void getLoanByExternalId_Success() {
        // Given
        String loanExternalId = "EXT-001";
        Boolean staffInSelectedOfficeOnly = false;
        String associations = "all";
        String exclude = "none";
        String fields = "id,accountNo";

        when(loansApi.retrieveLoanByExternalId(loanExternalId, staffInSelectedOfficeOnly, associations, exclude, fields))
            .thenReturn(Observable.just(mockLoanResponse));

        // When
        GetLoansLoanIdResponse response = fineractLoanService.getLoanByExternalId(loanExternalId, staffInSelectedOfficeOnly, associations, exclude, fields);

        // Then
        assertNotNull(response);
        verify(loansApi).retrieveLoanByExternalId(loanExternalId, staffInSelectedOfficeOnly, associations, exclude, fields);
    }

    @Test
    void performStateTransition_Success() {
        // Given
        Long loanId = 1L;
        String command = "approve";
        Map<String, Object> request = new HashMap<>();
        request.put("approvedOnDate", "2024-01-02");
        request.put("note", "Approved by manager");

        when(loansApi.stateTransitions(loanId, request, command))
            .thenReturn(Observable.just(mockStateTransitionResponse));

        // When
        PostLoansLoanIdResponse response = fineractLoanService.performStateTransition(loanId, request, command);

        // Then
        assertNotNull(response);
        verify(loansApi).stateTransitions(loanId, request, command);
    }

    @Test
    void performStateTransitionByExternalId_Success() {
        // Given
        String loanExternalId = "EXT-001";
        String command = "approve";
        Map<String, Object> request = new HashMap<>();
        request.put("approvedOnDate", "2024-01-02");
        request.put("note", "Approved by manager");

        when(loansApi.stateTransitionsByExternalId(loanExternalId, request, command))
            .thenReturn(Observable.just(mockStateTransitionResponse));

        // When
        PostLoansLoanIdResponse response = fineractLoanService.performStateTransitionByExternalId(loanExternalId, request, command);

        // Then
        assertNotNull(response);
        verify(loansApi).stateTransitionsByExternalId(loanExternalId, request, command);
    }

    @Test
    void modifyLoan_Success() {
        // Given
        Long loanId = 1L;
        String command = "update";
        Map<String, Object> request = new HashMap<>();
        request.put("principal", 15000.0);
        request.put("note", "Updated principal amount");

        PutLoansLoanIdResponse mockModifyResponse = new PutLoansLoanIdResponse();

        when(loansApi.modifyLoanApplication(loanId, request, command))
            .thenReturn(Observable.just(mockModifyResponse));

        // When
        PutLoansLoanIdResponse response = fineractLoanService.modifyLoan(loanId, request, command);

        // Then
        assertNotNull(response);
        verify(loansApi).modifyLoanApplication(loanId, request, command);
    }

    @Test
    void modifyLoanByExternalId_Success() {
        // Given
        String loanExternalId = "EXT-001";
        String command = "update";
        Map<String, Object> request = new HashMap<>();
        request.put("principal", 15000.0);
        request.put("note", "Updated principal amount");

        PutLoansLoanIdResponse mockModifyResponse = new PutLoansLoanIdResponse();

        when(loansApi.modifyLoanApplicationByExternalId(loanExternalId, request, command))
            .thenReturn(Observable.just(mockModifyResponse));

        // When
        PutLoansLoanIdResponse response = fineractLoanService.modifyLoanByExternalId(loanExternalId, request, command);

        // Then
        assertNotNull(response);
        verify(loansApi).modifyLoanApplicationByExternalId(loanExternalId, request, command);
    }

    @Test
    void deleteLoan_Success() {
        // Given
        Long loanId = 1L;
        DeleteLoansLoanIdResponse mockDeleteResponse = new DeleteLoansLoanIdResponse();

        when(loansApi.deleteLoanApplication(loanId))
            .thenReturn(Observable.just(mockDeleteResponse));

        // When
        DeleteLoansLoanIdResponse response = fineractLoanService.deleteLoan(loanId);

        // Then
        assertNotNull(response);
        verify(loansApi).deleteLoanApplication(loanId);
    }

    @Test
    void createLoan_ApiException_ThrowsFineractApiException() {
        // Given
        String command = "submit";
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "Bad Request");
        Response<?> response = Response.error(400, responseBody);
        HttpException httpException = new HttpException(response);

        when(loansApi.calculateLoanScheduleOrSubmitLoanApplication(validLoanRequest, command))
            .thenReturn(Observable.error(httpException));

        // When & Then
        assertThrows(FineractApiException.class, () -> {
            fineractLoanService.createLoan(validLoanRequest, command);
        });
        verify(loansApi).calculateLoanScheduleOrSubmitLoanApplication(validLoanRequest, command);
    }

    @Test
    void getLoan_ApiException_ThrowsFineractApiException() {
        // Given
        Long loanId = 1L;
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "Not Found");
        Response<?> response = Response.error(404, responseBody);
        HttpException httpException = new HttpException(response);

        when(loansApi.retrieveLoan(eq(loanId), any(), any(), any(), any()))
            .thenReturn(Observable.error(httpException));

        // When & Then
        assertThrows(FineractApiException.class, () -> {
            fineractLoanService.getLoan(loanId, false, "all", "none", "id,accountNo");
        });
        verify(loansApi).retrieveLoan(eq(loanId), any(), any(), any(), any());
    }

    @Test
    void performStateTransition_ApiException_ThrowsFineractApiException() {
        // Given
        Long loanId = 1L;
        String command = "approve";
        Map<String, Object> request = new HashMap<>();
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "Unauthorized");
        Response<?> response = Response.error(401, responseBody);
        HttpException httpException = new HttpException(response);

        when(loansApi.stateTransitions(loanId, request, command))
            .thenReturn(Observable.error(httpException));

        // When & Then
        assertThrows(FineractApiException.class, () -> {
            fineractLoanService.performStateTransition(loanId, request, command);
        });
        verify(loansApi).stateTransitions(loanId, request, command);
    }

    @Test
    void createLoan_WithDifferentCommands_Success() {
        // Given
        String[] commands = {"submit", "calculate", "preview"};

        for (String command : commands) {
            when(loansApi.calculateLoanScheduleOrSubmitLoanApplication(validLoanRequest, command))
                .thenReturn(Observable.just(mockCreateResponse));

            // When
            PostLoansResponse response = fineractLoanService.createLoan(validLoanRequest, command);

            // Then
            assertNotNull(response);
            verify(loansApi).calculateLoanScheduleOrSubmitLoanApplication(validLoanRequest, command);
        }
    }

    @Test
    void performStateTransition_WithDifferentCommands_Success() {
        // Given
        Long loanId = 1L;
        String[] commands = {"approve", "reject", "withdraw", "disburse", "undoapproval"};
        Map<String, Object> request = new HashMap<>();
        request.put("note", "State transition");

        for (String command : commands) {
            when(loansApi.stateTransitions(loanId, request, command))
                .thenReturn(Observable.just(mockStateTransitionResponse));

            // When
            PostLoansLoanIdResponse response = fineractLoanService.performStateTransition(loanId, request, command);

            // Then
            assertNotNull(response);
            verify(loansApi).stateTransitions(loanId, request, command);
        }
    }

    @Test
    void modifyLoan_WithNullCommand_Success() {
        // Given
        Long loanId = 1L;
        String command = null;
        Map<String, Object> request = new HashMap<>();
        request.put("principal", 15000.0);

        PutLoansLoanIdResponse mockModifyResponse = new PutLoansLoanIdResponse();

        when(loansApi.modifyLoanApplication(loanId, request, command))
            .thenReturn(Observable.just(mockModifyResponse));

        // When
        PutLoansLoanIdResponse response = fineractLoanService.modifyLoan(loanId, request, command);

        // Then
        assertNotNull(response);
        verify(loansApi).modifyLoanApplication(loanId, request, command);
    }

    @Test
    void getLoan_WithNullParameters_Success() {
        // Given
        Long loanId = 1L;
        when(loansApi.retrieveLoan(loanId, null, null, null, null))
            .thenReturn(Observable.just(mockLoanResponse));

        // When
        GetLoansLoanIdResponse response = fineractLoanService.getLoan(loanId, null, null, null, null);

        // Then
        assertNotNull(response);
        verify(loansApi).retrieveLoan(loanId, null, null, null, null);
    }
}
