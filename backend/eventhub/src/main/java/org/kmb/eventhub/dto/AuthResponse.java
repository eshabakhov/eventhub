package org.kmb.eventhub.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private UserDTO user;
    private Object customUser;
}
