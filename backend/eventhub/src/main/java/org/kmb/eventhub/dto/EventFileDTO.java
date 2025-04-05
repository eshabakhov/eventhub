package org.kmb.eventhub.dto;

import lombok.Data;

@Data
public class EventFileDTO {
    private Long fileId;
    private Long eventId;
    private String fileContent;
}
