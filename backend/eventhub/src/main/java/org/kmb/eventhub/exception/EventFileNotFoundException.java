package org.kmb.eventhub.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class EventFileNotFoundException extends EntityNotFoundException {
    public EventFileNotFoundException(Long fileId) {
        super(String.format("file %d not found", fileId));
    }
}
