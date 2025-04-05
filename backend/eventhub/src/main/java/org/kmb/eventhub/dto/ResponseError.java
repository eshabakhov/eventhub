package org.kmb.eventhub.dto;

import lombok.Data;

@Data
public class ResponseError {
    private Short httpCode;
    private String message;
}
