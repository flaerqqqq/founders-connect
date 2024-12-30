package com.example.foundersconnect.logging;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class HttpLogMessage {

    @NotEmpty
    String requestId;
    @NotNull
    RequestData requestData;
    @NotNull
    ResponseData responseData;

    @Value
    @Builder
    public static class RequestData {
        @NotNull
        Map<String,String> headers;
        String body;
    }

    @Value
    @Builder
    public static class ResponseData {
        int statusCode;
        @NotNull
        Map<String, String> headers;
        String body;
    }
}