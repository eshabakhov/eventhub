package org.kmb.eventhub.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.kmb.eventhub.dto.ResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Objects;

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

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode statusCode,
                                                                  @NonNull WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return Objects.requireNonNull(handleExceptionInternal(ex, ResponseDTO.getResponse(ex.getMessage()),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request));
    }

}
