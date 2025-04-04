package org.kmb.eventhub.dto;


import lombok.Data;
import org.kmb.eventhub.enums.EventFormat;
import java.time.LocalDate;

@Data
public class Event {
    private Long id;

    private String title;

    private String description;

    private EventFormat format;

    private String location;

    private LocalDate startDateTime;

    private LocalDate endDateTime;

    private Long organizerId;
}
