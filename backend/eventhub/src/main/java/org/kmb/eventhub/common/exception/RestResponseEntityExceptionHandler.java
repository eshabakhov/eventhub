package org.kmb.eventhub.common.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.kmb.eventhub.common.dto.ResponseDTO;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler(value = { RuntimeException.class })
    protected ResponseEntity<Object> handleRuntimeException(RuntimeException ex,
                                                               WebRequest request) {
        log.warn(ex.getMessage(), ex);
        return handleExceptionInternal(ex, ResponseDTO.getResponse(ex.getMessage()),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
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

    @NonNull
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
                                                        @NonNull HttpHeaders headers,
                                                        @NonNull HttpStatusCode status,
                                                        @NonNull WebRequest request) {
        Object[] args = new Object[]{ex.getPropertyName(), ex.getValue()};
        String var10000 = String.valueOf(args[0]);
        String defaultDetail = String.format("Failed to convert '%s' with value: '%s'", var10000, args[1]);
        log.warn(defaultDetail, ex);
        return handleExceptionInternal(ex, ResponseDTO.getResponse(defaultDetail),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
}
