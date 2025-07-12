package com.project.demo.logic.entity.http;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global response handler that wraps all HTTP responses in a unified format using {@link HttpResponse}.
 * Adds metadata to every response (method and URL), and ensures consistent structure across the application.
 * <p>
 * This handler helps enforce standardization and avoids repetition across controller responses.
 *
 * @author dgutierrez
 */
@RestControllerAdvice
public class GlobalResponseHandler {

    /**
     * Builds a full structured response with message, body, status and metadata generated from the request.
     *
     * @param message the message to include in the response
     * @param body the body content of the response
     * @param status the HTTP status code
     * @param request the HTTP request object used to generate metadata
     * @return a ResponseEntity wrapping the standardized HttpResponse
     * @param <T> the type of the response body
     *
     * @author dgutierrez
     */
    @ResponseBody
    public <T> ResponseEntity<HttpResponse<T>> handleResponse(
            String message,
            T body,
            HttpStatus status,
            HttpServletRequest request
    ) {
        Meta meta = buildMeta(request);
        return buildResponse(message, body, status, meta);
    }

    /**
     * Builds a structured response with only a message and status code, without body content.
     *
     * @param message the message to include in the response
     * @param status the HTTP status code
     * @param request the HTTP request object used to generate metadata
     * @return a ResponseEntity wrapping the standardized HttpResponse without body
     *
     * @author dgutierrez
     */
    @ResponseBody
    public ResponseEntity<HttpResponse<Object>> handleResponse(
            String message,
            HttpStatus status,
            HttpServletRequest request
    ) {
        Meta meta = buildMeta(request);
        return buildResponse(message, null, status, meta);
    }

    /**
     * Builds a structured response using custom metadata.
     *
     * @param message the message to include in the response
     * @param body the body content of the response
     * @param status the HTTP status code
     * @param meta the metadata to attach to the response
     * @return a ResponseEntity wrapping the standardized HttpResponse
     * @param <T> the type of the response body
     *
     * @author dgutierrez
     */
    @ResponseBody
    public <T> ResponseEntity<HttpResponse<T>> handleResponse(
            String message,
            T body,
            HttpStatus status,
            Meta meta
    ) {
        return buildResponse(message, body, status, meta);
    }

    /**
     * Creates metadata from the HTTP request method and URL.
     *
     * @param request the HTTP request
     * @return a new Meta instance
     *
     * @author dgutierrez
     */
    private Meta buildMeta(HttpServletRequest request) {
        return new Meta(request.getMethod(), request.getRequestURL().toString());
    }

    /**
     * Constructs a {@link HttpResponse} using the given data, handling already wrapped responses appropriately.
     *
     * @param message the response message
     * @param body the response body
     * @param status the HTTP status
     * @param meta metadata to include
     * @return the fully built ResponseEntity with wrapped HttpResponse
     * @param <T> the type of the response body
     *
     * @author dgutierrez
     */
    private <T> ResponseEntity<HttpResponse<T>> buildResponse(
            String message,
            T body,
            HttpStatus status,
            Meta meta
    ) {
        HttpResponse<T> response;

        if (body instanceof HttpResponse<?> existing) {
            //noinspection unchecked
            response = (HttpResponse<T>) existing;
            response.setMeta(meta);
        } else {
            response = new HttpResponse<>(message, body, meta);
        }

        return new ResponseEntity<>(response, status);
    }
}