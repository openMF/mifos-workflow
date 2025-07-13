package org.mifos.workflow.exception;

import lombok.Getter;
import retrofit2.HttpException;

/**
 * Custom exception for Fineract API errors.
 * Provides detailed error information including HTTP status, error body, and operation context.
 *
 * <p>This exception is thrown when operations against the Fineract API fail, providing
 * comprehensive error details for debugging and error handling.</p>
 */
@Getter
public class FineractApiException extends RuntimeException {

    private final int httpStatus;
    private final String errorBody;
    private final String operation;
    private final String resourceId;

    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_CONFLICT = 409;
    public static final int STATUS_INTERNAL_SERVER_ERROR = 500;

    public FineractApiException(String message, HttpException httpException, String operation, String resourceId, String errorBody) {
        super(message, httpException);
        this.httpStatus = httpException.code();
        this.operation = operation;
        this.resourceId = resourceId;
        this.errorBody = errorBody;
    }


    public FineractApiException(String message, Throwable cause, String operation) {
        super(message, cause);
        this.httpStatus = -1;
        this.operation = operation;
        this.resourceId = null;
        this.errorBody = cause.getMessage();
    }

    public FineractApiException(String message, Throwable cause, String operation, String resourceId) {
        super(message, cause);
        this.httpStatus = -1;
        this.operation = operation;
        this.resourceId = resourceId;
        this.errorBody = cause.getMessage();
    }


    public boolean isBadRequest() {
        return httpStatus == STATUS_BAD_REQUEST;
    }


    public boolean isUnauthorized() {
        return httpStatus == STATUS_UNAUTHORIZED;
    }


    public boolean isForbidden() {
        return httpStatus == STATUS_FORBIDDEN;
    }

    public boolean isNotFound() {
        return httpStatus == STATUS_NOT_FOUND;
    }


    public boolean isConflict() {
        return httpStatus == STATUS_CONFLICT;
    }


    public boolean isServerError() {
        return httpStatus >= STATUS_INTERNAL_SERVER_ERROR;
    }


    public boolean isClientError() {
        return httpStatus >= STATUS_BAD_REQUEST && httpStatus < STATUS_INTERNAL_SERVER_ERROR;
    }


    public boolean hasHttpStatus() {
        return httpStatus > 0;
    }

    @Override
    public String toString() {
        return String.format("FineractApiException{httpStatus=%d, operation='%s', resourceId='%s', message='%s'}", httpStatus, operation, resourceId, getMessage());
    }
}