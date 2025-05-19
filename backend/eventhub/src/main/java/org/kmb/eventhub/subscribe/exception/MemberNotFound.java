package org.kmb.eventhub.subscribe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class MemberNotFound extends RuntimeException {
    public MemberNotFound(Long userId) {
        super(String.format("Member %d not found", userId));
    }
}
