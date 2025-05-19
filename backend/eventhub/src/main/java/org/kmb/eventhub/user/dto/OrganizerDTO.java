package org.kmb.eventhub.user.dto;

import lombok.Data;

@Data
public class OrganizerDTO {
    private Long id;
    private String name;
    private String description;
    private String industry;
    private String address;
    private Boolean isAccredited;
}