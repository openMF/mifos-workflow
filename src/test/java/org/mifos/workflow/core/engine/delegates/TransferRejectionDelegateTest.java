package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.workflow.dto.fineract.client.ClientRejectTransferRequestDTO;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.mifos.fineract.client.models.PostClientsClientIdResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferRejectionDelegateTest {

    @Mock
    private FineractClientService fineractClientService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private TransferRejectionDelegate delegate;

    @Test
    void execute_Success() {
        when(execution.getVariable("clientId")).thenReturn(66L);
        when(execution.getVariable("rejectionReason")).thenReturn("Policy violation");
        PostClientsClientIdResponse response = new PostClientsClientIdResponse();
        when(fineractClientService.rejectClientTransfer(eq(66L), eq("rejectTransfer"), any(ClientRejectTransferRequestDTO.class)))
            .thenReturn(io.reactivex.rxjava3.core.Observable.just(response));

        delegate.execute(execution);

        verify(execution).setVariable("transferRejected", true);
        verify(execution).setVariable("transferStatus", "REJECTED");
    }

    @Test
    void execute_ApiError_Propagates() {
        when(execution.getVariable("clientId")).thenReturn(66L);
        when(execution.getVariable("rejectionReason")).thenReturn("x");
        when(fineractClientService.rejectClientTransfer(eq(66L), eq("rejectTransfer"), any(ClientRejectTransferRequestDTO.class)))
            .thenReturn(io.reactivex.rxjava3.core.Observable.error(new FineractApiException("bad", new RuntimeException("bad"), "rejectTransfer", "66")));
        assertThrows(FineractApiException.class, () -> delegate.execute(execution));
    }
}


