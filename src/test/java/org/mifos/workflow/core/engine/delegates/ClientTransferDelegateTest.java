package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.workflow.dto.fineract.client.ClientTransferRequestDTO;
import org.mifos.fineract.client.models.PostClientsClientIdResponse;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientTransferDelegateTest {

    @Mock
    private FineractClientService fineractClientService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private ClientTransferDelegate delegate;

    @Test
    void execute_Success() {
        when(execution.getVariable("clientId")).thenReturn(55L);
        when(execution.getVariable("destinationOfficeId")).thenReturn(77L);
        when(execution.getVariable("effectiveDate")).thenReturn(LocalDate.parse("2025-01-01"));
        when(execution.getVariable("dateFormat")).thenReturn("yyyy-MM-dd");
        when(execution.getVariable("locale")).thenReturn("en");
        PostClientsClientIdResponse response = new PostClientsClientIdResponse();
        when(fineractClientService.proposeClientTransfer(eq(55L), any(ClientTransferRequestDTO.class)))
                .thenReturn(io.reactivex.rxjava3.core.Observable.just(response));

        delegate.execute(execution);

        verify(execution).setVariable("transferProposed", true);
        verify(execution).setVariable("transferStatus", "PROPOSED");
    }

    @Test
    void execute_ApiError_Propagates() {
        when(execution.getVariable("clientId")).thenReturn(55L);
        when(execution.getVariable("destinationOfficeId")).thenReturn(77L);
        when(execution.getVariable("effectiveDate")).thenReturn(LocalDate.now());
        when(fineractClientService.proposeClientTransfer(eq(55L), any(ClientTransferRequestDTO.class)))
                .thenReturn(io.reactivex.rxjava3.core.Observable.error(new FineractApiException("bad", new RuntimeException("bad"), "propose", "55")));
        assertThrows(FineractApiException.class, () -> delegate.execute(execution));
    }
}


