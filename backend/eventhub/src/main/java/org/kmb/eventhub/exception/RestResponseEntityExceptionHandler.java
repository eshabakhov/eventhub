package org.kmb.eventhub.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.kmb.eventhub.dto.ResponseDTO;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@Slf4j
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { EntityNotFoundException.class })
    protected ResponseEntity<Object> handleNotFound(RuntimeException ex,
                                                    WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return handleExceptionInternal(ex, ResponseDTO.getResponse(ex.getMessage()),
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = { UnexpectedException.class })
    protected ResponseEntity<Object> handleUnexpectedException(RuntimeException ex,
                                                                WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return handleExceptionInternal(ex, ResponseDTO.getResponse(ex.getMessage()),
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @NonNull
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode statusCode,
                                                                  @NonNull WebRequest request) {
        String errorMessage = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        log.warn(errorMessage, ex);
        return handleExceptionInternal(ex, ResponseDTO.getResponse(errorMessage),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @NonNull
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode statusCode,
                                                                  @NonNull WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return handleExceptionInternal(ex, ResponseDTO.getResponse(ex.getMessage()),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
}
