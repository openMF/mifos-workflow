package org.mifos.workflow.util;

import lombok.extern.slf4j.Slf4j;
import org.mifos.workflow.exception.FineractApiException;
import retrofit2.HttpException;

/**
 * Utility class for handling Fineract API errors consistently across the application.
 * Provides methods to extract error details, log errors, and create appropriate exceptions.
 */
@Slf4j
public class FineractErrorHandler {


    public static String extractErrorBody(HttpException httpError) {
        try {
            if (httpError.response() != null && httpError.response().errorBody() != null) {
                okhttp3.ResponseBody responseBody = httpError.response().errorBody();
                if (responseBody != null) {
                    String errorBody = responseBody.string();
                    log.debug("Extracted error body: {}", errorBody);
                    return errorBody;
                }
            }
        } catch (Exception e) {
            log.warn("Could not read error response body: {}", e.getMessage());
        }
        return "";
    }


    public static void logDetailedError(String operation, String resourceId, Throwable error) {
        if (error instanceof HttpException httpError) {
            if (resourceId != null) {
                log.error("Fineract API error during {} for resource {}: HTTP {} - {}", operation, resourceId, httpError.code(), httpError.message());
            } else {
                log.error("Fineract API error during {}: HTTP {} - {}", operation, httpError.code(), httpError.message());
            }
        } else {
            if (resourceId != null) {
                log.error("Error during {} for resource {}: {}", operation, resourceId, error.getMessage(), error);
            } else {
                log.error("Error during {}: {}", operation, error.getMessage(), error);
            }
        }
    }

    public static FineractApiException createException(String operation, Throwable error, String resourceId) {
        if (error instanceof HttpException httpError) {
            String errorBody = extractErrorBody(httpError);
            return createException(operation, error, resourceId, errorBody);
        } else {
            String message = resourceId != null ? String.format("Failed to %s for resource %s", operation, resourceId) : String.format("Failed to %s", operation);

            return new FineractApiException(message, error, operation);
        }
    }

    public static FineractApiException createException(String operation, Throwable error, String resourceId, String errorBody) {
        if (error instanceof HttpException httpError) {
            String message = resourceId != null ? String.format("Failed to %s for resource %s: HTTP %d", operation, resourceId, httpError.code()) : String.format("Failed to %s: HTTP %d", operation, httpError.code());

            log.debug("Creating FineractApiException with errorBody: {}", errorBody);
            return new FineractApiException(message, httpError, operation, resourceId, errorBody);
        } else {
            String message = resourceId != null ? String.format("Failed to %s for resource %s", operation, resourceId) : String.format("Failed to %s", operation);

            return new FineractApiException(message, error, operation);
        }
    }


    public static FineractApiException handleError(String operation, Throwable error, String resourceId) {
        if (error instanceof HttpException httpError) {
            String errorBody = extractErrorBody(httpError);
            logDetailedError(operation, resourceId, error);
            return createException(operation, error, resourceId, errorBody);
        } else {
            logDetailedError(operation, resourceId, error);
            return createException(operation, error, resourceId);
        }
    }


    public static FineractApiException handleError(String operation, Throwable error) {
        return handleError(operation, error, null);
    }

    public static void logErrorWithBody(String operation, String resourceId, Throwable error) {
        if (error instanceof HttpException httpError) {
            String errorBody = extractErrorBody(httpError);
            if (resourceId != null) {
                log.error("Fineract API error during {} for resource {}: HTTP {} - {} - Body: {}", operation, resourceId, httpError.code(), httpError.message(), errorBody);
            } else {
                log.error("Fineract API error during {}: HTTP {} - {} - Body: {}", operation, httpError.code(), httpError.message(), errorBody);
            }
        } else {
            logDetailedError(operation, resourceId, error);
        }
    }
} 