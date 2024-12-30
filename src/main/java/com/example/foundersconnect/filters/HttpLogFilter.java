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

/**
 * A filter that logs HTTP request and response data for specific URIs.
 * <p>
 * This filter extends {@link OncePerRequestFilter} to ensure that the filter is executed only once per request.
 * It wraps the request and response in {@link ContentCachingRequestWrapper} and {@link ContentCachingResponseWrapper}
 * respectively to capture the content for logging purposes.
 * </p>
 * <p>
 * The filter logs the HTTP data using the {@link HttpLogMessage} class and the {@link ObjectMapper} for JSON serialization.
 * </p>
 * <p>
 * The filter skips logging for URIs that start with "/api/v1/auth" or do not start with "/api/v1/".
 * </p>
 */
@Slf4j
public class HttpLogFilter extends OncePerRequestFilter {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AUTH_URI = "/api/v1/auth";
    private static final String URI_PREFIX = "/api/v1/";

    /**
     * Filters the request and response, logging the HTTP data if the URI matches the specified patterns.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if an error occurs during filtering
     * @throws IOException if an I/O error occurs during filtering
     */
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

    /**
     * Logs the HTTP request and response data.
     *
     * @param requestWrapper the wrapped HTTP request
     * @param responseWrapper the wrapped HTTP response
     * @throws IOException if an I/O error occurs during logging
     */
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

    /**
     * Retrieves the headers from the HTTP request.
     *
     * @param requestWrapper the wrapped HTTP request
     * @return a map of header names and values
     */
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

    /**
     * Retrieves the headers from the HTTP response.
     *
     * @param responseWrapper the wrapped HTTP response
     * @return a map of header names and values
     */
    private @NotNull Map<String, String> getHeaders(ContentCachingResponseWrapper responseWrapper) {
        return responseWrapper.getHeaderNames().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        responseWrapper::getHeader,
                        (existing, replacement) -> existing
                ));
    }
}