package org.kmb.eventhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
public class ImmutableFieldException extends RuntimeException {
    public ImmutableFieldException(String field) {
        super(String.format("%s field immutable", field));
    }
}
