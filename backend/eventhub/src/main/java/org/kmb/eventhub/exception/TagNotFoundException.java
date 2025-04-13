package org.kmb.eventhub.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class TagNotFoundException extends EntityNotFoundException {
    public TagNotFoundException(Long tagId) {
        super(String.format("Tag %d not found", tagId));
    }
}
