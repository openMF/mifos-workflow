package org.mifos.workflow.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.workflow.dto.fineract.auth.AuthenticationRequest;
import org.mifos.workflow.dto.fineract.auth.AuthenticationResponse;
import org.mifos.workflow.dto.fineract.auth.AuthenticationStatusDTO;
import org.mifos.workflow.exception.FineractApiException;
import org.mifos.workflow.service.fineract.auth.FineractAuthService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.mifos.workflow.util.ApiResponse;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.HttpException;
import retrofit2.Response;
import okhttp3.MediaType;
import okhttp3.ResponseBody;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private FineractAuthService fineractAuthService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private AuthenticationRequest validRequest;
    private AuthenticationResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = AuthenticationRequest.builder().username("testuser").password("testpass").build();

        validResponse = AuthenticationResponse.builder().username("testuser").base64EncodedAuthenticationKey("test-token").authenticated(true).build();
    }

    @Test
    void authenticate_Success() {
        // Given
        when(fineractAuthService.authenticate(any(AuthenticationRequest.class))).thenReturn(Observable.just(validResponse));

        // When
        ResponseEntity<AuthenticationResponse> response = authenticationController.authenticate(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("test-token", response.getBody().getBase64EncodedAuthenticationKey());
        verify(fineractAuthService).authenticate(validRequest);
    }

    @Test
    void authenticate_InvalidRequest_ReturnsBadRequest() {
        // Given
        when(fineractAuthService.authenticate(any(AuthenticationRequest.class))).thenThrow(new IllegalArgumentException("Invalid credentials"));

        // When
        ResponseEntity<AuthenticationResponse> response = authenticationController.authenticate(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(fineractAuthService).authenticate(validRequest);
    }

    @Test
    void authenticate_FineractApiException_ReturnsUnauthorized() {
        // Given
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "Authentication failed");
        Response<?> response = Response.error(401, responseBody);
        HttpException httpException = new HttpException(response);

        when(fineractAuthService.authenticate(any(AuthenticationRequest.class))).thenThrow(new FineractApiException("Authentication failed", httpException, "authentication", "testuser", "Authentication failed"));

        // When
        ResponseEntity<AuthenticationResponse> responseEntity = authenticationController.authenticate(validRequest);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        verify(fineractAuthService).authenticate(validRequest);
    }

    @Test
    void authenticate_UnexpectedException_ReturnsInternalServerError() {
        // Given
        when(fineractAuthService.authenticate(any(AuthenticationRequest.class))).thenThrow(new RuntimeException("Unexpected error"));

        // When
        ResponseEntity<AuthenticationResponse> response = authenticationController.authenticate(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(fineractAuthService).authenticate(validRequest);
    }

    @Test
    void getAuthenticationStatus_Authenticated_ReturnsTrue() {
        // Given
        when(fineractAuthService.isAuthenticated()).thenReturn(true);

        // When
        ResponseEntity<AuthenticationStatusDTO> response = authenticationController.getAuthenticationStatus();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isAuthenticated());
        verify(fineractAuthService).isAuthenticated();
    }

    @Test
    void getAuthenticationStatus_NotAuthenticated_ReturnsFalse() {
        // Given
        when(fineractAuthService.isAuthenticated()).thenReturn(false);

        // When
        ResponseEntity<AuthenticationStatusDTO> response = authenticationController.getAuthenticationStatus();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isAuthenticated());
        verify(fineractAuthService).isAuthenticated();
    }

    @Test
    void logout_Success() {
        // Given
        doNothing().when(fineractAuthService).clearCachedAuthKey();

        // When
        ResponseEntity<ApiResponse<Void>> response = authenticationController.logout();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Logged out successfully", response.getBody().getMessage());
        verify(fineractAuthService).clearCachedAuthKey();
    }
}