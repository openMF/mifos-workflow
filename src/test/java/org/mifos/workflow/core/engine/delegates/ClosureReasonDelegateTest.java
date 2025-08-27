package org.mifos.workflow.core.engine.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.service.fineract.client.FineractClientService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClosureReasonDelegateTest {

    @Mock
    private FineractClientService fineractClientService;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private ClosureReasonDelegate delegate;

    @Test
    void execute_Success() {
        when(fineractClientService.retrieveClientClosureReasons())
                .thenReturn(io.reactivex.rxjava3.core.Observable.just(List.of()));

        delegate.execute(execution);

        verify(execution).setVariable(eq("closureReasonsFetched"), eq(true));
        verify(execution).setVariable(eq("closureReasons"), any());
    }

    @Test
    void execute_ApiError_Propagates() {
        when(fineractClientService.retrieveClientClosureReasons())
                .thenReturn(io.reactivex.rxjava3.core.Observable.error(new FineractApiException("bad", new RuntimeException("bad"), "reasons", "0")));
        assertThrows(FineractApiException.class, () -> delegate.execute(execution));
    }
}




