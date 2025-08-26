package org.mifos.workflow.exception;

import org.junit.jupiter.api.Test;
import retrofit2.HttpException;
import retrofit2.Response;
import okhttp3.MediaType;
import okhttp3.ResponseBody;

import static org.junit.jupiter.api.Assertions.*;

class FineractApiExceptionTest {

    @Test
    void constructor_WithHttpException_SetsFieldsCorrectly() {
        // Given
        String message = "API call failed";
        HttpException httpException = createHttpException(400, "Bad Request Body");
        String operation = "createLoan";
        String resourceId = "123";
        String errorBody = "Invalid loan parameters";

        // When
        FineractApiException exception = new FineractApiException(message, httpException, operation, resourceId, errorBody);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(httpException, exception.getCause());
        assertEquals(operation, exception.getOperation());
        assertEquals(resourceId, exception.getResourceId());
        assertEquals(errorBody, exception.getErrorBody());
        assertEquals(400, exception.getHttpStatus());
        assertTrue(exception.isClientError());
        assertFalse(exception.isServerError());
    }

    @Test
    void constructor_WithThrowable_SetsFallbackFields() {
        // Given
        String message = "API error";
        RuntimeException cause = new RuntimeException("Root cause message");
        String operation = "getClient";

        // When
        FineractApiException exception = new FineractApiException(message, cause, operation);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(operation, exception.getOperation());
        assertNull(exception.getResourceId());
        assertEquals("Root cause message", exception.getErrorBody());
        assertEquals(-1, exception.getHttpStatus());
        assertFalse(exception.hasHttpStatus());
    }

    @Test
    void constructor_WithThrowableAndResource_SetsResourceId() {
        // Given
        String message = "API error";
        IllegalArgumentException cause = new IllegalArgumentException("Illegal argument");
        String operation = "updateClient";
        String resourceId = "456";

        // When
        FineractApiException exception = new FineractApiException(message, cause, operation, resourceId);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(operation, exception.getOperation());
        assertEquals(resourceId, exception.getResourceId());
        assertEquals("Illegal argument", exception.getErrorBody());
        assertEquals(-1, exception.getHttpStatus());
    }

    @Test
    void toString_ReturnsValidString() {
        // Given
        HttpException httpException = createHttpException(404, "Not Found");
        FineractApiException exception = new FineractApiException("API call failed", httpException, "retrieveLoan", "789", "Not Found body");

        // When
        String result = exception.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("FineractApiException"));
        assertTrue(result.contains("retrieveLoan"));
        assertTrue(result.contains("789"));
    }

    private HttpException createHttpException(int statusCode, String body) {
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), body);
        Response<?> response = Response.error(statusCode, responseBody);
        return new HttpException(response);
    }
}
