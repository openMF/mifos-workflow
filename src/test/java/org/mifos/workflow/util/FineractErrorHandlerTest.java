package org.mifos.workflow.util;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mifos.workflow.exception.FineractApiException;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.HttpException;
import retrofit2.Response;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class FineractErrorHandlerTest {

    @Test
    void extractErrorBody_HttpExceptionWithEmptyBody_ReturnsEmptyString() {
        // Given
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "");
        Response<Object> response = Response.error(500, responseBody);
        HttpException httpException = new HttpException(response);

        // When
        String result = FineractErrorHandler.extractErrorBody(httpException);

        // Then
        assertEquals("", result);
    }

    @Test
    void extractErrorBody_HttpExceptionWithValidBody_ReturnsBody() {
        // Given
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "{\"error\": \"Test error\"}");
        Response<Object> response = Response.error(500, responseBody);
        HttpException httpException = new HttpException(response);

        // When
        String result = FineractErrorHandler.extractErrorBody(httpException);

        // Then
        assertEquals("{\"error\": \"Test error\"}", result);
    }

    @Test
    void createException_HttpExceptionWithResourceId_ReturnsCorrectException() {
        // Given
        String operation = "GET_CLIENT";
        String resourceId = "client-123";
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "Client not found");
        Response<?> response = Response.error(404, responseBody);
        HttpException httpException = new HttpException(response);

        // When
        FineractApiException result = FineractErrorHandler.createException(operation, httpException, resourceId);

        // Then
        assertEquals("Failed to GET_CLIENT for resource client-123: HTTP 404", result.getMessage());
        assertEquals(operation, result.getOperation());
        assertEquals(resourceId, result.getResourceId());
        assertEquals(404, result.getHttpStatus());
    }

    @Test
    void createException_HttpExceptionWithoutResourceId_ReturnsCorrectException() {
        // Given
        String operation = "GET_CLIENT";
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "Client not found");
        Response<?> response = Response.error(404, responseBody);
        HttpException httpException = new HttpException(response);

        // When
        FineractApiException result = FineractErrorHandler.createException(operation, httpException, null);

        // Then
        assertEquals("Failed to GET_CLIENT: HTTP 404", result.getMessage());
        assertEquals(operation, result.getOperation());
        assertNull(result.getResourceId());
        assertEquals(404, result.getHttpStatus());
    }

    @Test
    void createException_NonHttpExceptionWithResourceId_ReturnsCorrectException() {
        // Given
        String operation = "GET_CLIENT";
        String resourceId = "client-123";
        RuntimeException runtimeException = new RuntimeException("Network error");

        // When
        FineractApiException result = FineractErrorHandler.createException(operation, runtimeException, resourceId);

        // Then
        assertEquals("Failed to GET_CLIENT for resource client-123", result.getMessage());
        assertEquals(operation, result.getOperation());
        assertEquals(resourceId, result.getResourceId());
        assertEquals(-1, result.getHttpStatus());
    }

    @Test
    void createException_NonHttpExceptionWithoutResourceId_ReturnsCorrectException() {
        // Given
        String operation = "GET_CLIENT";
        RuntimeException runtimeException = new RuntimeException("Network error");

        // When
        FineractApiException result = FineractErrorHandler.createException(operation, runtimeException, null);

        // Then
        assertEquals("Failed to GET_CLIENT", result.getMessage());
        assertEquals(operation, result.getOperation());
        assertNull(result.getResourceId());
        assertEquals(-1, result.getHttpStatus());
    }

    @Test
    void createException_WithErrorBody_HttpExceptionWithResourceId_ReturnsCorrectException() {
        // Given
        String operation = "GET_CLIENT";
        String resourceId = "client-123";
        String errorBody = "{\"error\": \"Client not found\"}";
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), errorBody);
        Response<?> response = Response.error(404, responseBody);
        HttpException httpException = new HttpException(response);

        // When
        FineractApiException result = FineractErrorHandler.createException(operation, httpException, resourceId, errorBody);

        // Then
        assertEquals("Failed to GET_CLIENT for resource client-123: HTTP 404", result.getMessage());
        assertEquals(operation, result.getOperation());
        assertEquals(resourceId, result.getResourceId());
        assertEquals(404, result.getHttpStatus());
    }

    @Test
    void createException_WithErrorBody_HttpExceptionWithoutResourceId_ReturnsCorrectException() {
        // Given
        String operation = "GET_CLIENT";
        String errorBody = "{\"error\": \"Client not found\"}";
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), errorBody);
        Response<?> response = Response.error(404, responseBody);
        HttpException httpException = new HttpException(response);

        // When
        FineractApiException result = FineractErrorHandler.createException(operation, httpException, null, errorBody);

        // Then
        assertEquals("Failed to GET_CLIENT: HTTP 404", result.getMessage());
        assertEquals(operation, result.getOperation());
        assertNull(result.getResourceId());
        assertEquals(404, result.getHttpStatus());
    }

    @Test
    void createException_WithErrorBody_NonHttpExceptionWithResourceId_ReturnsCorrectException() {
        // Given
        String operation = "GET_CLIENT";
        String resourceId = "client-123";
        String errorBody = "Network error";
        RuntimeException runtimeException = new RuntimeException("Network error");

        // When
        FineractApiException result = FineractErrorHandler.createException(operation, runtimeException, resourceId, errorBody);

        // Then
        assertEquals("Failed to GET_CLIENT for resource client-123", result.getMessage());
        assertEquals(operation, result.getOperation());
        assertEquals(resourceId, result.getResourceId());
        assertEquals(-1, result.getHttpStatus());
    }

    @Test
    void createException_WithErrorBody_NonHttpExceptionWithoutResourceId_ReturnsCorrectException() {
        // Given
        String operation = "GET_CLIENT";
        String errorBody = "Network error";
        RuntimeException runtimeException = new RuntimeException("Network error");

        // When
        FineractApiException result = FineractErrorHandler.createException(operation, runtimeException, null, errorBody);

        // Then
        assertEquals("Failed to GET_CLIENT", result.getMessage());
        assertEquals(operation, result.getOperation());
        assertNull(result.getResourceId());
        assertEquals(-1, result.getHttpStatus());
    }

    @Test
    void handleError_HttpExceptionWithResourceId_ReturnsCorrectException() {
        // Given
        String operation = "GET_CLIENT";
        String resourceId = "client-123";
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "Client not found");
        Response<?> response = Response.error(404, responseBody);
        HttpException httpException = new HttpException(response);

        // When
        FineractApiException result = FineractErrorHandler.handleError(operation, httpException, resourceId);

        // Then
        assertEquals("Failed to GET_CLIENT for resource client-123: HTTP 404", result.getMessage());
        assertEquals(operation, result.getOperation());
        assertEquals(resourceId, result.getResourceId());
        assertEquals(404, result.getHttpStatus());
    }

    @Test
    void handleError_HttpExceptionWithoutResourceId_ReturnsCorrectException() {
        // Given
        String operation = "GET_CLIENT";
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "Client not found");
        Response<?> response = Response.error(404, responseBody);
        HttpException httpException = new HttpException(response);

        // When
        FineractApiException result = FineractErrorHandler.handleError(operation, httpException, null);

        // Then
        assertEquals("Failed to GET_CLIENT: HTTP 404", result.getMessage());
        assertEquals(operation, result.getOperation());
        assertNull(result.getResourceId());
        assertEquals(404, result.getHttpStatus());
    }

    @Test
    void handleError_NonHttpExceptionWithResourceId_ReturnsCorrectException() {
        // Given
        String operation = "GET_CLIENT";
        String resourceId = "client-123";
        RuntimeException runtimeException = new RuntimeException("Network error");

        // When
        FineractApiException result = FineractErrorHandler.handleError(operation, runtimeException, resourceId);

        // Then
        assertEquals("Failed to GET_CLIENT for resource client-123", result.getMessage());
        assertEquals(operation, result.getOperation());
        assertEquals(resourceId, result.getResourceId());
        assertEquals(-1, result.getHttpStatus());
    }

    @Test
    void handleError_NonHttpExceptionWithoutResourceId_ReturnsCorrectException() {
        // Given
        String operation = "GET_CLIENT";
        RuntimeException runtimeException = new RuntimeException("Network error");

        // When
        FineractApiException result = FineractErrorHandler.handleError(operation, runtimeException, null);

        // Then
        assertEquals("Failed to GET_CLIENT", result.getMessage());
        assertEquals(operation, result.getOperation());
        assertNull(result.getResourceId());
        assertEquals(-1, result.getHttpStatus());
    }

    @Test
    void handleError_WithoutResourceId_ReturnsCorrectException() {
        // Given
        String operation = "GET_CLIENT";
        RuntimeException runtimeException = new RuntimeException("Network error");

        // When
        FineractApiException result = FineractErrorHandler.handleError(operation, runtimeException);

        // Then
        assertEquals("Failed to GET_CLIENT", result.getMessage());
        assertEquals(operation, result.getOperation());
        assertNull(result.getResourceId());
        assertEquals(-1, result.getHttpStatus());
    }

    @Test
    void logDetailedError_HttpExceptionWithResourceId_DoesNotThrowException() {
        // Given
        String operation = "GET_CLIENT";
        String resourceId = "client-123";
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "Client not found");
        Response<?> response = Response.error(404, responseBody);
        HttpException httpException = new HttpException(response);

        // When & Then - should not throw any exception
        assertDoesNotThrow(() -> {
            FineractErrorHandler.logDetailedError(operation, resourceId, httpException);
        });
    }

    @Test
    void logDetailedError_HttpExceptionWithoutResourceId_DoesNotThrowException() {
        // Given
        String operation = "GET_CLIENT";
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "Client not found");
        Response<?> response = Response.error(404, responseBody);
        HttpException httpException = new HttpException(response);

        // When & Then - should not throw any exception
        assertDoesNotThrow(() -> {
            FineractErrorHandler.logDetailedError(operation, null, httpException);
        });
    }

    @Test
    void logDetailedError_NonHttpExceptionWithResourceId_DoesNotThrowException() {
        // Given
        String operation = "GET_CLIENT";
        String resourceId = "client-123";
        RuntimeException runtimeException = new RuntimeException("Network error");

        // When & Then - should not throw any exception
        assertDoesNotThrow(() -> {
            FineractErrorHandler.logDetailedError(operation, resourceId, runtimeException);
        });
    }

    @Test
    void logDetailedError_NonHttpExceptionWithoutResourceId_DoesNotThrowException() {
        // Given
        String operation = "GET_CLIENT";
        RuntimeException runtimeException = new RuntimeException("Network error");

        // When & Then - should not throw any exception
        assertDoesNotThrow(() -> {
            FineractErrorHandler.logDetailedError(operation, null, runtimeException);
        });
    }

    @Test
    void logErrorWithBody_HttpExceptionWithResourceId_DoesNotThrowException() {
        // Given
        String operation = "GET_CLIENT";
        String resourceId = "client-123";
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "Client not found");
        Response<?> response = Response.error(404, responseBody);
        HttpException httpException = new HttpException(response);

        // When & Then - should not throw any exception
        assertDoesNotThrow(() -> {
            FineractErrorHandler.logErrorWithBody(operation, resourceId, httpException);
        });
    }

    @Test
    void logErrorWithBody_HttpExceptionWithoutResourceId_DoesNotThrowException() {
        // Given
        String operation = "GET_CLIENT";
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "Client not found");
        Response<?> response = Response.error(404, responseBody);
        HttpException httpException = new HttpException(response);

        // When & Then - should not throw any exception
        assertDoesNotThrow(() -> {
            FineractErrorHandler.logErrorWithBody(operation, null, httpException);
        });
    }

    @Test
    void logErrorWithBody_NonHttpExceptionWithResourceId_DoesNotThrowException() {
        // Given
        String operation = "GET_CLIENT";
        String resourceId = "client-123";
        RuntimeException runtimeException = new RuntimeException("Network error");

        // When & Then - should not throw any exception
        assertDoesNotThrow(() -> {
            FineractErrorHandler.logErrorWithBody(operation, resourceId, runtimeException);
        });
    }

    @Test
    void logErrorWithBody_NonHttpExceptionWithoutResourceId_DoesNotThrowException() {
        // Given
        String operation = "GET_CLIENT";
        RuntimeException runtimeException = new RuntimeException("Network error");

        // When & Then - should not throw any exception
        assertDoesNotThrow(() -> {
            FineractErrorHandler.logErrorWithBody(operation, null, runtimeException);
        });
    }
}