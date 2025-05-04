package org.kmb.eventhub.event.dto;

import lombok.Data;

@Data
public class EventLinkDTO {
    private Long linkId;
    private Long eventId;
    private String link;
}
