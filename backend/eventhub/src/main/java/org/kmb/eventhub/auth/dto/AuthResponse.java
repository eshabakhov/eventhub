package org.kmb.eventhub.auth.dto;

import lombok.Data;
import org.kmb.eventhub.user.dto.UserDTO;

@Data
public class AuthResponse {
    private UserDTO user;
    private Object customUser;
}
