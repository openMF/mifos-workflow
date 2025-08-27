package org.mifos.workflow.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;

/*
 * Generic API response wrapper.
 * @param <T> the type of the response data
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Instant timestamp;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data, Instant timestamp) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, Instant.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}


