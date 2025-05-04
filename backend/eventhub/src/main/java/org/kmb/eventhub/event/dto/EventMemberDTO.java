package org.kmb.eventhub.event.dto;

import lombok.Data;

@Data
public class EventMemberDTO {

    private Long eventId;
    private Long userId;
}
