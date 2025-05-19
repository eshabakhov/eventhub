package org.kmb.eventhub.event.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class EventIdMatchException extends RuntimeException {
    public EventIdMatchException(String message) {
        super(message);
    }
}
