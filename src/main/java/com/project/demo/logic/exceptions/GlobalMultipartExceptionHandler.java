package com.project.demo.logic.exceptions;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

/**
 * Handler global para excepciones relacionadas con la subida de archivos multipart.
 * Maneja específicamente:
 * - MaxUploadSizeExceededException: cuando el tamaño del archivo subido excede el límite configurado.
 * - MultipartException: para manejar errores generales de multipart.
 * - IllegalStateException: para capturar excepciones de estado ilegal, verificando si la causa es FileSizeLimitExceededException.
 * @author dgutierrez
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalMultipartExceptionHandler {

    private final GlobalResponseHandler response = new GlobalResponseHandler();

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return response.badRequest("La imagen supera el tamaño máximo permitido (1 MB).", request);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<?> handleMultipart(MultipartException ex, HttpServletRequest request) {
        return response.badRequest("La imagen supera el tamaño máximo permitido (1 MB).", request);
    }

    // Con esto si se lanza otra IllegalStateException se deja pasar a la cadena por si hay otro handler
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        Throwable cause = ex.getCause();
        if (cause instanceof FileSizeLimitExceededException) {
            return response.badRequest("La imagen supera el tamaño máximo permitido (1 MB).", request);
        }
        //Ahora si el cause no era FileSizeLimitExceededException, relanza la excepción porque deay, probablemente hay
        //otro handler para IllegalStateException por ahi configurado
        throw ex;
    }
}