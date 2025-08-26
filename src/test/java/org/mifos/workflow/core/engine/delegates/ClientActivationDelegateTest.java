package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.fineract.client.models.PostClientsClientIdResponse;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientActivationDelegateTest {

    @Mock
    private FineractClientService fineractClientService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private ClientActivationDelegate delegate;

    @Test
    void execute_Success() {
        Long clientId = 33L;
        when(execution.getProcessInstanceId()).thenReturn("p-2");
        when(execution.getVariable("clientId")).thenReturn(clientId);
        when(execution.getVariable("activationDate")).thenReturn(LocalDate.parse("2024-01-15"));

        PostClientsClientIdResponse response = mock(PostClientsClientIdResponse.class);
        when(response.getResourceId()).thenReturn(clientId);
        when(fineractClientService.activateClient(eq(clientId), any(LocalDate.class), anyString(), anyString()))
                .thenReturn(io.reactivex.rxjava3.core.Observable.just(response));

        delegate.execute(execution);

        verify(execution).setVariable(eq("clientActivated"), eq(true));
        verify(execution).setVariable(eq("clientStatus"), eq("ACTIVE"));
        verify(execution).setVariable(eq("activationDate"), any(LocalDate.class));
    }

    @Test
    void execute_FineractError_Propagates() {
        Long clientId = 33L;
        when(execution.getProcessInstanceId()).thenReturn("p-2");
        when(execution.getVariable("clientId")).thenReturn(clientId);
        when(execution.getVariable("activationDate")).thenReturn(LocalDate.now());

        FineractApiException apiEx = new FineractApiException("bad", new RuntimeException("bad"), "activate client", clientId.toString());
        when(fineractClientService.activateClient(eq(clientId), any(LocalDate.class), anyString(), anyString()))
                .thenReturn(io.reactivex.rxjava3.core.Observable.error(apiEx));

        assertThrows(FineractApiException.class, () -> delegate.execute(execution));
    }

    @Test
    void execute_MissingClient_ThrowsWorkflowException() {
        when(execution.getProcessInstanceId()).thenReturn("p-2");
        when(execution.getVariable("clientId")).thenReturn(null);
        WorkflowException ex = assertThrows(WorkflowException.class, () -> delegate.execute(execution));
        assertTrue(ex.getMessage().contains("Client activation failed"));
    }
}


