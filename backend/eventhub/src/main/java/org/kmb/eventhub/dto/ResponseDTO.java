package org.kmb.eventhub.dto;

import lombok.Data;

@Data
public class ResponseDTO {
    private String message;

    public static ResponseDTO getResponse(String message) {
        ResponseDTO response = new ResponseDTO();
        response.setMessage(message);
        return response;
    }
}
