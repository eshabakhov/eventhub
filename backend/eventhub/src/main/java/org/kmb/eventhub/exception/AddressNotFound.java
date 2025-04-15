package org.kmb.eventhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AddressNotFound extends RuntimeException {
    public AddressNotFound(String address) {
        super(String.format("%s not found", address));
    }
}
