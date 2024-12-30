package com.example.foundersconnect.filters;

import com.example.foundersconnect.logging.HttpLogMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class HttpLogFilter extends OncePerRequestFilter {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AUTH_URI = "/api/v1/auth";
    private static final String URI_PREFIX = "/api/v1/";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith(AUTH_URI) || !request.getRequestURI().startsWith(URI_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            logHttpData(requestWrapper, responseWrapper);
        }

    }

    private void logHttpData(ContentCachingRequestWrapper requestWrapper,
                             ContentCachingResponseWrapper responseWrapper) throws IOException {
        HttpLogMessage.RequestData requestData = HttpLogMessage.RequestData.builder()
                .headers(getHeaders(requestWrapper))
                .body(new String(requestWrapper.getContentAsByteArray()))
                .build();
        HttpLogMessage.ResponseData responseData = HttpLogMessage.ResponseData.builder()
                .statusCode(responseWrapper.getStatus())
                .headers(getHeaders(responseWrapper))
                .body(new String(responseWrapper.getContentAsByteArray()))
                .build();
        HttpLogMessage httpLogMessage = HttpLogMessage.builder()
                .requestId(requestWrapper.getRequestId())
                .requestData(requestData)
                .responseData(responseData)
                .build();
        log.info(objectMapper.writeValueAsString(httpLogMessage));
        responseWrapper.copyBodyToResponse();
    }

    private Map<String, String> getHeaders(ContentCachingRequestWrapper requestWrapper) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = requestWrapper.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = requestWrapper.getHeader(headerName);
            headers.put(headerName, headerValue);
        }
        return headers;
    }

    private @NotNull Map<String, String> getHeaders(ContentCachingResponseWrapper responseWrapper) {
        return responseWrapper.getHeaderNames().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        responseWrapper::getHeader,
                        (existing, replacement) -> existing
                ));
    }
}