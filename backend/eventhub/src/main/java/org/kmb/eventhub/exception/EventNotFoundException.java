package org.kmb.eventhub.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)

public class EventNotFoundException extends EntityNotFoundException {
  public EventNotFoundException(Long eventId) {
    super(String.format("Event %d not found", eventId));
  }
}
