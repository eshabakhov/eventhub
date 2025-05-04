package org.kmb.eventhub.subscribe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class EventMemberNotFound extends RuntimeException {
    public EventMemberNotFound(Long userId, Long eventId) {
        super(String.format("User %d is not a member of event %d", userId, eventId));
    }
}
