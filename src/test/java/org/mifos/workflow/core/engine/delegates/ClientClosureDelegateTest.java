package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.workflow.dto.fineract.client.ClientCloseRequestDTO;
import org.mifos.fineract.client.models.PostClientsClientIdResponse;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientClosureDelegateTest {

    @Mock
    private FineractClientService fineractClientService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private ClientClosureDelegate delegate;

    @Test
    void execute_Success() {
        when(execution.getVariable("clientId")).thenReturn(5L);
        when(execution.getVariable("closureReasonId")).thenReturn(2L);
        when(execution.getVariable("dateFormat")).thenReturn("yyyy-MM-dd");
        when(execution.getVariable("locale")).thenReturn("en");

        PostClientsClientIdResponse response = new PostClientsClientIdResponse();
        when(fineractClientService.closeClient(eq(5L), eq("close"), any(ClientCloseRequestDTO.class)))
                .thenReturn(io.reactivex.rxjava3.core.Observable.just(response));

        delegate.execute(execution);

        verify(execution).setVariable("clientClosed", true);
    }

    @Test
    void execute_ApiError_Propagates() {
        when(execution.getVariable("clientId")).thenReturn(5L);
        when(execution.getVariable("closureReasonId")).thenReturn(2L);
        when(fineractClientService.closeClient(eq(5L), eq("close"), any(ClientCloseRequestDTO.class)))
                .thenReturn(io.reactivex.rxjava3.core.Observable.error(new FineractApiException("bad", new RuntimeException("bad"), "close", "5")));
        assertThrows(FineractApiException.class, () -> delegate.execute(execution));
    }

    @Test
    void execute_MissingVars_ThrowsWorkflowException() {
        when(execution.getVariable("clientId")).thenReturn(null);
        assertThrows(WorkflowException.class, () -> delegate.execute(execution));
    }
}


