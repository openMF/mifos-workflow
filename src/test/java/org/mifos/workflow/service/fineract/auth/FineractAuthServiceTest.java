package org.mifos.workflow.service.fineract.auth;

import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.fineract.client.models.PostAuthenticationRequest;
import org.mifos.fineract.client.models.PostAuthenticationResponse;
import org.mifos.workflow.api.auth.AuthenticationApi;
import org.mifos.workflow.dto.fineract.auth.AuthenticationRequest;
import org.mifos.workflow.dto.fineract.auth.AuthenticationResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.HttpException;
import retrofit2.Response;
import okhttp3.MediaType;
import okhttp3.ResponseBody;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FineractAuthServiceTest {

    @Mock
    private AuthenticationApi authenticationApi;

    @InjectMocks
    private FineractAuthService authService;

    private AuthenticationRequest validRequest;
    private PostAuthenticationResponse successResponse;

    @BeforeEach
    void setUp() {
        validRequest = AuthenticationRequest.builder()
                .username("testuser")
                .password("testpass")
                .build();
    }

    @Test
    void authenticate_Success() throws Exception {
        // Arrange
        successResponse = mock(PostAuthenticationResponse.class);
        when(successResponse.getBase64EncodedAuthenticationKey()).thenReturn("test-key");
        when(successResponse.getAuthenticated()).thenReturn(true);
        when(successResponse.getUsername()).thenReturn("testuser");
        
        when(authenticationApi.authenticate(any(PostAuthenticationRequest.class), anyBoolean()))
                .thenReturn(Observable.just(successResponse));

        // Act
        CompletableFuture<AuthenticationResponse> future = authService.authenticate(validRequest);
        AuthenticationResponse response = future.get(5, TimeUnit.SECONDS);

        // Assert
        assertNotNull(response);
        assertEquals("test-key", response.getBase64EncodedAuthenticationKey());
        assertTrue(response.isAuthenticated());
        assertEquals("testuser", response.getUsername());
        verify(authenticationApi).authenticate(any(PostAuthenticationRequest.class), anyBoolean());
    }

    @Test
    void authenticate_NullRequest_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.authenticate(null));
    }

    @Test
    void authenticate_EmptyCredentials_ThrowsException() {
        // Arrange
        AuthenticationRequest emptyRequest = AuthenticationRequest.builder()
                .username("")
                .password("")
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.authenticate(emptyRequest));
    }

    @Test
    void authenticate_ApiError_ThrowsException() {
        // Arrange
        ResponseBody errorBody = ResponseBody.create(MediaType.parse("application/json"), "Unauthorized");
        Response<Void> errorResponse = Response.error(401, errorBody);
        when(authenticationApi.authenticate(any(PostAuthenticationRequest.class), anyBoolean()))
                .thenReturn(Observable.error(new HttpException(errorResponse)));

        // Act & Assert
        assertThrows(ExecutionException.class, () -> {
            CompletableFuture<AuthenticationResponse> future = authService.authenticate(validRequest);
            future.get(5, TimeUnit.SECONDS);
        });
    }

    @Test
    void authenticate_NetworkError_ThrowsException() {
        // Arrange
        when(authenticationApi.authenticate(any(PostAuthenticationRequest.class), anyBoolean()))
                .thenReturn(Observable.error(new RuntimeException("Network error")));

        // Act & Assert
        assertThrows(ExecutionException.class, () -> {
            CompletableFuture<AuthenticationResponse> future = authService.authenticate(validRequest);
            future.get(5, TimeUnit.SECONDS);
        });
    }


    @Test
    void authenticate_ConcurrentRequests_HandlesCorrectly() throws Exception {
        // Arrange
        successResponse = mock(PostAuthenticationResponse.class);
        when(successResponse.getBase64EncodedAuthenticationKey()).thenReturn("test-key");
        when(successResponse.getAuthenticated()).thenReturn(true);
        when(successResponse.getUsername()).thenReturn("testuser");
        
        when(authenticationApi.authenticate(any(PostAuthenticationRequest.class), anyBoolean()))
                .thenReturn(Observable.just(successResponse));

        // Act
        CompletableFuture<AuthenticationResponse> future1 = authService.authenticate(validRequest);
        CompletableFuture<AuthenticationResponse> future2 = authService.authenticate(validRequest);

        // Assert
        AuthenticationResponse response1 = future1.get(5, TimeUnit.SECONDS);
        AuthenticationResponse response2 = future2.get(5, TimeUnit.SECONDS);

        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(response1.getBase64EncodedAuthenticationKey(), response2.getBase64EncodedAuthenticationKey());
    }
}
