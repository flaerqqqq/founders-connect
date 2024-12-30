package com.example.foundersconnect.logging;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * Represents an HTTP log message containing request and response data.
 */
@Value
@Builder
public class HttpLogMessage {

    /**
     * The unique identifier for the request.
     */
    @NotEmpty
    String requestId;

    /**
     * The data related to the HTTP request.
     */
    @NotNull
    RequestData requestData;

    /**
     * The data related to the HTTP response.
     */
    @NotNull
    ResponseData responseData;

    /**
     * Represents the data related to an HTTP request.
     */
    @Value
    @Builder
    public static class RequestData {

        /**
         * The headers of the HTTP request.
         */
        @NotNull
        Map<String, String> headers;

        /**
         * The body of the HTTP request.
         */
        String body;
    }

    /**
     * Represents the data related to an HTTP response.
     */
    @Value
    @Builder
    public static class ResponseData {

        /**
         * The status code of the HTTP response.
         */
        int statusCode;

        /**
         * The headers of the HTTP response.
         */
        @NotNull
        Map<String, String> headers;

        /**
         * The body of the HTTP response.
         */
        String body;
    }
}