package org.kmb.eventhub.event.dto;

import lombok.Data;

@Data
public class EventFileDTO {
    private Long fileId;
    private Long eventId;
    private byte[] fileContent;
    private String fileName;
    private String fileType;
    private Long fileSize;
}
