package com.dmitriy.seatflow.common.error;

import java.time.Instant;
import java.util.List;

public class ApiErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final ApiErrorCode code;
    private final String message;
    private final String path;
    private final List<FieldErrorResponse> fieldErrors;

    public ApiErrorResponse(
            Instant timestamp,
            int status,
            ApiErrorCode code,
            String message,
            String path,
            List<FieldErrorResponse> fieldErrors
    ) {
        this.timestamp = timestamp;
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public ApiErrorCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public List<FieldErrorResponse> getFieldErrors() {
        return fieldErrors;
    }
}
