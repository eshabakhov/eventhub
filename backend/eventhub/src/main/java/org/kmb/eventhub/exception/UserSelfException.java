package org.kmb.eventhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UserSelfException extends IllegalArgumentException {
    public UserSelfException(Long id) {
        super(String.format("The user with %d is not allowed any operation to himself", id));
    }
}
