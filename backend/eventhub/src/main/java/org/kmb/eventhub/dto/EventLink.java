package org.kmb.eventhub.dto;

import lombok.Data;

@Data
public class EventLink {
    private Long linkId;
    private Long eventId;
    private String link;
}
