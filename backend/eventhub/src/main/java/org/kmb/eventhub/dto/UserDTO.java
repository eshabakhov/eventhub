package org.kmb.eventhub.dto;

import lombok.Data;
import org.kmb.eventhub.enums.RoleEnum;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String displayName;
    private String password;
    private String email;
    private Boolean isActive;
    private RoleEnum role;
}
