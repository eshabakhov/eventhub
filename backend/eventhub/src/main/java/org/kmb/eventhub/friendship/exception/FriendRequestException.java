package org.kmb.eventhub.friendship.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class FriendRequestException extends RuntimeException {
    public FriendRequestException(String message) {
        super(message);
    }
}
