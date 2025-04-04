﻿package org.kmb.eventhub.dto;


import lombok.Data;
import org.kmb.eventhub.enums.RoleEnum;

@Data
public class User {

    private Long id;

    private String username;

    private String displayName;

    private String password;

    private String email;

    private boolean isActive;

    private RoleEnum role;
}
