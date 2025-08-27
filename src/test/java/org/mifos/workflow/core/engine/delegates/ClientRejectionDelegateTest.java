package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.fineract.client.models.PostClientsClientIdResponse;
import org.mifos.workflow.dto.fineract.client.ClientRejectRequestDTO;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.HttpException;
import retrofit2.Response;
import okhttp3.MediaType;
import okhttp3.ResponseBody;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientRejectionDelegateTest {

    @Mock
    private FineractClientService fineractClientService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private ClientRejectionDelegate delegate;

    @Test
    void execute_Success() {
        Long clientId = 99L;
        when(execution.getProcessInstanceId()).thenReturn("p-cr");
        when(execution.getVariable("clientId")).thenReturn(clientId);
        when(execution.getVariable("rejectionReason")).thenReturn("Incomplete docs");
        when(execution.getVariable("rejectionDate")).thenReturn(LocalDate.parse("2024-05-01"));
        when(execution.getVariable("rejectionReasonId")).thenReturn(1L);

        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getResourceId()).thenReturn(clientId);
        when(fineractClientService.rejectClient(eq(clientId), eq("reject"), any(ClientRejectRequestDTO.class)))
                .thenReturn(io.reactivex.rxjava3.core.Observable.just(response));

        delegate.execute(execution);

        verify(execution).setVariable("clientRejected", true);
        verify(execution).setVariable("clientStatus", "REJECTED");
        verify(execution).setVariable(eq("rejectionDate"), any(LocalDate.class));
        verify(execution).setVariable(eq("rejectionReason"), any());
    }

    @Test
    void execute_FineractError_NotFoundReason_MarksRejected() {
        Long clientId = 99L;
        when(execution.getProcessInstanceId()).thenReturn("p-cr");
        when(execution.getVariable("clientId")).thenReturn(clientId);
        when(execution.getVariable("rejectionReason")).thenReturn("Invalid reason");
        when(execution.getVariable("rejectionDate")).thenReturn(LocalDate.parse("2024-05-01"));
        when(execution.getVariable("rejectionReasonId")).thenReturn(123L);

        String errorBody = "ClientRejectReason with id 123 does not exist";
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), errorBody);
        Response<?> response = Response.error(404, responseBody);
        HttpException httpException = new HttpException(response);
        FineractApiException apiEx = new FineractApiException("ClientRejectReason with id 123 does not exist", httpException, "reject", clientId.toString(), errorBody);
        when(fineractClientService.rejectClient(eq(clientId), eq("reject"), any(ClientRejectRequestDTO.class)))
                .thenReturn(io.reactivex.rxjava3.core.Observable.error(apiEx));

        assertDoesNotThrow(() -> delegate.execute(execution));

        // Verify that the delegate set the expected variables when handling the error gracefully
        verify(execution).setVariable("clientRejected", true);
        verify(execution).setVariable("clientStatus", "REJECTED");
        verify(execution).setVariable(eq("rejectionDate"), any(LocalDate.class));
        verify(execution).setVariable(eq("rejectionReason"), any());
        verify(execution).setVariable(eq("errorMessage"), any());
    }
}


