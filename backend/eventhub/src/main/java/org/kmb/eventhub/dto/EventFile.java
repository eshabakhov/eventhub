package org.kmb.eventhub.dto;

import lombok.Data;

@Data
public class EventFile {
    private Long fileId;
    private Long eventId;
    private String fileContent;
}
