package org.kmb.eventhub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.kmb.eventhub.enums.RoleEnum;

@Data
public class UserDTO {
    private Long id;
    private String username;
    @JsonProperty(value = "displayName")
    private String displayName;
    private String password;
    private String email;
    @JsonProperty(value = "isActive")
    private boolean isActive;
    private RoleEnum role;
}
