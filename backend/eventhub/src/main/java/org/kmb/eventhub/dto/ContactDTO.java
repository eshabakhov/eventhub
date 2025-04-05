package org.kmb.eventhub.dto;

import lombok.Data;

@Data
public class ContactDTO {
    private Long contactId;
    private String info;
    private Long organizerId;
}
