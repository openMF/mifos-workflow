package org.mifos.workflow.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import retrofit2.HttpException;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler that provides consistent error responses across the application.
 * Converts exceptions to RFC 7807 Problem Details format.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String ERROR_BASE_URL = "https://mifos.org/errors/";
    private static final String GENERIC_ERROR_MESSAGE = "An unexpected error occurred";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation exception occurred: {}", ex.getMessage(), ex);

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed for the provided data");

        problemDetail.setType(URI.create(ERROR_BASE_URL + "validation-failed"));
        problemDetail.setTitle("VALIDATION_FAILED");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false));
        problemDetail.setProperty("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument exception occurred: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());

        problemDetail.setType(URI.create(ERROR_BASE_URL + "invalid-argument"));
        problemDetail.setTitle("INVALID_ARGUMENT");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false));

        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        log.error("Illegal state exception occurred: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());

        problemDetail.setType(URI.create(ERROR_BASE_URL + "invalid-state"));
        problemDetail.setTitle("INVALID_STATE");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Runtime exception occurred: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR_MESSAGE);

        problemDetail.setType(URI.create(ERROR_BASE_URL + "internal-server-error"));
        problemDetail.setTitle("INTERNAL_SERVER_ERROR");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false));

        return ResponseEntity.internalServerError().body(problemDetail);
    }

    @ExceptionHandler(FineractApiException.class)
    public ResponseEntity<ProblemDetail> handleFineractApiException(FineractApiException ex, WebRequest request) {
        log.error("Fineract API exception occurred during {}: {}", ex.getOperation(), ex.getMessage(), ex);
        log.debug("FineractApiException errorBody: '{}'", ex.getErrorBody());

        HttpStatus status = mapFineractHttpStatus(ex.getHttpStatus());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());

        problemDetail.setType(URI.create(ERROR_BASE_URL + "fineract-api-error"));
        problemDetail.setTitle("FINERACT_API_ERROR");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false));
        problemDetail.setProperty("operation", ex.getOperation());

        String errorBody = ex.getErrorBody();
        if (errorBody != null && !errorBody.trim().isEmpty()) {
            problemDetail.setProperty("errorBody", errorBody);
            log.debug("Set errorBody in ProblemDetail: '{}'", errorBody);
        } else {
            problemDetail.setProperty("errorBody", "No error body available");
            log.debug("No error body available, setting default message");
        }

        if (ex.getResourceId() != null) {
            problemDetail.setProperty("resourceId", ex.getResourceId());
        }

        log.debug("Returning ProblemDetail with errorBody: '{}'", problemDetail.getProperties().get("errorBody"));
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(WorkflowException.class)
    public ResponseEntity<ProblemDetail> handleWorkflowException(WorkflowException ex, WebRequest request) {
        log.error("Workflow exception occurred during {}: {}", ex.getOperation(), ex.getMessage(), ex);

        HttpStatus status = ex.getErrorCode() != null ? mapWorkflowErrorCode(ex.getErrorCode()) : HttpStatus.INTERNAL_SERVER_ERROR;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());

        problemDetail.setType(URI.create(ERROR_BASE_URL + "workflow-error"));
        problemDetail.setTitle("WORKFLOW_ERROR");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false));
        problemDetail.setProperty("operation", ex.getOperation());

        if (ex.getErrorCode() != null) {
            problemDetail.setProperty("errorCode", ex.getErrorCode());
        }
        if (ex.getProcessId() != null) {
            problemDetail.setProperty("processId", ex.getProcessId());
        }
        if (ex.getTaskId() != null) {
            problemDetail.setProperty("taskId", ex.getTaskId());
        }

        return ResponseEntity.status(status).body(problemDetail);
    }


    @ExceptionHandler(HttpException.class)
    public ResponseEntity<ProblemDetail> handleHttpException(HttpException ex, WebRequest request) {
        log.error("HTTP exception occurred: {} - {}", ex.code(), ex.message(), ex);

        HttpStatus status = mapFineractHttpStatus(ex.code());

        String errorBody = "";
        try {
            if (ex.response().errorBody() != null) {
                errorBody = ex.response().errorBody().string();
            }
        } catch (Exception e) {
            log.warn("Could not read error response body", e);
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.message());

        problemDetail.setType(URI.create(ERROR_BASE_URL + "http-error"));
        problemDetail.setTitle("HTTP_ERROR");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false));
        problemDetail.setProperty("errorBody", errorBody);

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, WebRequest request) {
        log.error("Generic exception occurred: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR_MESSAGE);

        problemDetail.setType(URI.create(ERROR_BASE_URL + "generic-error"));
        problemDetail.setTitle("GENERIC_ERROR");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getDescription(false));

        return ResponseEntity.internalServerError().body(problemDetail);
    }

    private HttpStatus mapFineractHttpStatus(int fineractStatus) {
        return switch (fineractStatus) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            case 422 -> HttpStatus.UNPROCESSABLE_ENTITY;
            case 500, 502, 503, 504 -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private HttpStatus mapWorkflowErrorCode(String errorCode) {
        return switch (errorCode) {
            case WorkflowException.ERROR_PROCESS_NOT_FOUND, WorkflowException.ERROR_TASK_NOT_FOUND,
                 WorkflowException.ERROR_PROCESS_DEFINITION_NOT_FOUND, WorkflowException.ERROR_DEPLOYMENT_NOT_FOUND ->
                    HttpStatus.NOT_FOUND;
            case WorkflowException.ERROR_INVALID_PROCESS_STATE, WorkflowException.ERROR_INVALID_TASK_STATE ->
                    HttpStatus.CONFLICT;
            case "VALIDATION_FAILED" -> HttpStatus.BAD_REQUEST;
            case "AUTHENTICATION_REQUIRED" -> HttpStatus.UNAUTHORIZED;
            case "PERMISSION_DENIED" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
} 