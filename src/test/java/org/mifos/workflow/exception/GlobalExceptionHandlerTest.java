package org.mifos.workflow.exception;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import retrofit2.HttpException;
import retrofit2.Response;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        webRequest = new ServletWebRequest(new MockHttpServletRequest());
    }

    @Test
    void handleValidationException_Success() throws NoSuchMethodException {
        // Given
        FieldError fieldError1 = new FieldError("client", "firstName", "First name is required");
        FieldError fieldError2 = new FieldError("client", "lastName", "Last name is required");

        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        Method testMethod = GlobalExceptionHandlerTest.class.getDeclaredMethod("testMethodWithParams", String.class, Integer.class);
        MethodParameter methodParameter = new MethodParameter(testMethod, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleValidationException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        ProblemDetail problemDetail = response.getBody();
        assertEquals("VALIDATION_FAILED", problemDetail.getTitle());
        assertEquals("Validation failed for the provided data", problemDetail.getDetail());
        assertEquals("https://mifos.org/errors/validation-failed", problemDetail.getType().toString());

        @SuppressWarnings("unchecked") Map<String, String> fieldErrors = (Map<String, String>) problemDetail.getProperties().get("fieldErrors");
        assertNotNull(fieldErrors);
        assertEquals("First name is required", fieldErrors.get("firstName"));
        assertEquals("Last name is required", fieldErrors.get("lastName"));
    }

    private void testMethodWithParams(String param1, Integer param2) {
    }

    @Test
    void handleIllegalArgumentException_Success() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid client ID");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleIllegalArgumentException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        ProblemDetail problemDetail = response.getBody();
        assertEquals("INVALID_ARGUMENT", problemDetail.getTitle());
        assertEquals("Invalid client ID", problemDetail.getDetail());
        assertEquals("https://mifos.org/errors/invalid-argument", problemDetail.getType().toString());
    }

    @Test
    void handleIllegalStateException_Success() {
        // Given
        IllegalStateException ex = new IllegalStateException("Process already completed");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleIllegalStateException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());

        ProblemDetail problemDetail = response.getBody();
        assertEquals("INVALID_STATE", problemDetail.getTitle());
        assertEquals("Process already completed", problemDetail.getDetail());
        assertEquals("https://mifos.org/errors/invalid-state", problemDetail.getType().toString());
    }

    @Test
    void handleRuntimeException_Success() {
        // Given
        RuntimeException ex = new RuntimeException("Unexpected error occurred");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleRuntimeException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());

        ProblemDetail problemDetail = response.getBody();
        assertEquals("INTERNAL_SERVER_ERROR", problemDetail.getTitle());
        assertEquals("An unexpected error occurred", problemDetail.getDetail());
        assertEquals("https://mifos.org/errors/internal-server-error", problemDetail.getType().toString());
    }

    @Test
    void handleFineractApiException_Success() {
        // Given
        HttpException httpException = new HttpException(Response.error(404, ResponseBody.create(MediaType.parse("application/json"), "Client with ID 123 not found")));
        FineractApiException ex = new FineractApiException("Client not found", httpException, "GET_CLIENT", "123", "Client with ID 123 not found");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleFineractApiException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());

        ProblemDetail problemDetail = response.getBody();
        assertEquals("FINERACT_API_ERROR", problemDetail.getTitle());
        assertEquals("Client not found", problemDetail.getDetail());
        assertEquals("https://mifos.org/errors/fineract-api-error", problemDetail.getType().toString());
        assertEquals("GET_CLIENT", problemDetail.getProperties().get("operation"));
        assertEquals("123", problemDetail.getProperties().get("resourceId"));
        assertEquals("Client with ID 123 not found", problemDetail.getProperties().get("errorBody"));
    }

    @Test
    void handleFineractApiException_WithNullErrorBody() {
        // Given
        HttpException httpException = new HttpException(Response.error(401, ResponseBody.create(MediaType.parse("application/json"), "Authentication failed")));
        FineractApiException ex = new FineractApiException("Authentication failed", httpException, "AUTHENTICATE", null, null);

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleFineractApiException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());

        ProblemDetail problemDetail = response.getBody();
        assertEquals("No error body available", problemDetail.getProperties().get("errorBody"));
    }

    @Test
    void handleWorkflowException_Success() {
        // Given
        WorkflowException ex = new WorkflowException("Process not found", "GET_PROCESS", "PROCESS_NOT_FOUND", "process-123", "task-456");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleWorkflowException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());

        ProblemDetail problemDetail = response.getBody();
        assertEquals("WORKFLOW_ERROR", problemDetail.getTitle());
        assertEquals("Process not found", problemDetail.getDetail());
        assertEquals("https://mifos.org/errors/workflow-error", problemDetail.getType().toString());
        assertEquals("PROCESS_NOT_FOUND", problemDetail.getProperties().get("errorCode"));
        assertEquals("process-123", problemDetail.getProperties().get("processId"));
        assertEquals("task-456", problemDetail.getProperties().get("taskId"));
        assertEquals("GET_PROCESS", problemDetail.getProperties().get("operation"));
    }

    @Test
    void handleWorkflowException_WithNullErrorCode() {
        // Given
        WorkflowException ex = new WorkflowException("Unknown workflow error", "UNKNOWN_OPERATION", null, null, null);

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleWorkflowException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());

        ProblemDetail problemDetail = response.getBody();
        assertEquals("WORKFLOW_ERROR", problemDetail.getTitle());
        assertNull(problemDetail.getProperties().get("errorCode"));
        assertEquals("UNKNOWN_OPERATION", problemDetail.getProperties().get("operation"));
    }

    @Test
    void handleHttpException_Success() {
        // Given
        Response<?> response = Response.error(500, ResponseBody.create(MediaType.parse("application/json"), "Internal server error"));
        HttpException ex = new HttpException(response);

        // When
        ResponseEntity<ProblemDetail> result = globalExceptionHandler.handleHttpException(ex, webRequest);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());

        ProblemDetail problemDetail = result.getBody();
        assertEquals("HTTP_ERROR", problemDetail.getTitle());
        assertEquals("https://mifos.org/errors/http-error", problemDetail.getType().toString());
    }

    @Test
    void handleGenericException_Success() {
        // Given
        Exception ex = new Exception("Generic error occurred");

        // When
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleGenericException(ex, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());

        ProblemDetail problemDetail = response.getBody();
        assertEquals("GENERIC_ERROR", problemDetail.getTitle());
        assertEquals("An unexpected error occurred", problemDetail.getDetail());
        assertEquals("https://mifos.org/errors/generic-error", problemDetail.getType().toString());
    }
}