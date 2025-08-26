package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.fineract.client.models.PostClientsResponse;
import org.mifos.workflow.dto.fineract.client.ClientCreateRequestDTO;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.exception.WorkflowException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientCreationDelegateTest {

    @Mock
    private FineractClientService fineractClientService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private ClientCreationDelegate delegate;

    @Test
    void execute_Success() {
        when(execution.getProcessInstanceId()).thenReturn("p-1");
        when(execution.getVariable("firstName")).thenReturn("John");
        when(execution.getVariable("lastName")).thenReturn("Doe");
        when(execution.getVariable("mobileNo")).thenReturn("+1000000");
        when(execution.getVariable("officeId")).thenReturn(1L);
        when(execution.getVariable("legalFormId")).thenReturn(1L);
        when(execution.getVariable("externalId")).thenReturn("EXT-1");
        when(execution.getVariable("dateOfBirth")).thenReturn(LocalDate.parse("1990-01-01"));
        when(execution.getVariable("dateFormat")).thenReturn("yyyy-MM-dd");
        when(execution.getVariable("locale")).thenReturn("en");
        when(execution.getVariable("active")).thenReturn(false);
        when(execution.getVariable("addressJson")).thenReturn(null);

        PostClientsResponse resp = mock(PostClientsResponse.class);
        when(resp.getClientId()).thenReturn(123L);
        when(fineractClientService.createClient(any(ClientCreateRequestDTO.class), anyString(), anyString(), anyLong()))
            .thenReturn(io.reactivex.rxjava3.core.Observable.just(resp));

        delegate.execute(execution);

        verify(fineractClientService).createClient(any(ClientCreateRequestDTO.class), eq("yyyy-MM-dd"), eq("en"), eq(1L));
        verify(execution).setVariable(eq("clientId"), eq(123L));
        verify(execution).setVariable(eq("clientCreated"), eq(true));
        verify(execution).setVariable(eq("clientStatus"), eq("PENDING"));
        verify(execution).setVariable(eq("creationDate"), any());
    }

    @Test
    void execute_FineractApiError_Propagates() {
        when(execution.getProcessInstanceId()).thenReturn("p-1");
        when(execution.getVariable("firstName")).thenReturn("John");
        when(execution.getVariable("lastName")).thenReturn("Doe");
        when(execution.getVariable("mobileNo")).thenReturn("+1000000");
        when(execution.getVariable("officeId")).thenReturn(1L);
        when(execution.getVariable("legalFormId")).thenReturn(1L);
        when(execution.getVariable("externalId")).thenReturn("EXT-1");
        when(execution.getVariable("dateOfBirth")).thenReturn(LocalDate.parse("1990-01-01"));
        when(execution.getVariable("dateFormat")).thenReturn("yyyy-MM-dd");
        when(execution.getVariable("locale")).thenReturn("en");
        when(execution.getVariable("active")).thenReturn(false);
        when(execution.getVariable("addressJson")).thenReturn(null);

        FineractApiException apiEx = new FineractApiException("bad", new RuntimeException("bad"), "create client", "1");
        when(fineractClientService.createClient(any(ClientCreateRequestDTO.class), anyString(), anyString(), anyLong()))
            .thenReturn(io.reactivex.rxjava3.core.Observable.error(apiEx));

        assertThrows(FineractApiException.class, () -> delegate.execute(execution));
    }

    @Test
    void execute_MissingRequired_ThrowsWorkflowException() {
        when(execution.getProcessInstanceId()).thenReturn("p-1");
        when(execution.getVariable("firstName")).thenReturn(null);
        WorkflowException ex = assertThrows(WorkflowException.class, () -> delegate.execute(execution));
        assertTrue(ex.getMessage().contains("Client creation failed"));
    }
}


