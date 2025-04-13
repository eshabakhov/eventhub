package org.kmb.eventhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class EmailFormatException extends IllegalArgumentException {
    public EmailFormatException(String email) {
        super(String.format("Email %s is not valid", email));
    }
}
