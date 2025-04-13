package org.kmb.eventhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class MissingFieldException extends IllegalArgumentException {
    public MissingFieldException(String field) {
        super(String.format("Missing field: %s", field));
    }
}
