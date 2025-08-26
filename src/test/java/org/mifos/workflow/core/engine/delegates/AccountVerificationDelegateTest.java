package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.fineract.client.models.GetClientsClientIdAccountsResponse;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountVerificationDelegateTest {

    @Mock
    private FineractClientService fineractClientService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private AccountVerificationDelegate delegate;

    @Test
    void execute_SetsFlagsBasedOnAccounts() {
        when(execution.getVariable("clientId")).thenReturn(11L);

        GetClientsClientIdAccountsResponse resp = mock(GetClientsClientIdAccountsResponse.class);
        when(resp.getLoanAccounts()).thenReturn(Set.of());

        when(fineractClientService.retrieveClientAccounts(11L))
            .thenReturn(io.reactivex.rxjava3.core.Observable.just(resp));

        delegate.execute(execution);

        verify(execution).setVariable(eq("accountsVerified"), eq(true));
        verify(execution).setVariable(eq("hasActiveLoans"), any());
    }

    @Test
    void execute_ApiError_Propagates() {
        when(execution.getVariable("clientId")).thenReturn(11L);
        when(fineractClientService.retrieveClientAccounts(11L))
            .thenReturn(io.reactivex.rxjava3.core.Observable.error(new FineractApiException("bad", new RuntimeException("bad"), "accounts", "11")));
        assertThrows(FineractApiException.class, () -> delegate.execute(execution));
    }
}


