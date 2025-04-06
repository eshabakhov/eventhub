package org.kmb.eventhub.dto;

import lombok.Data;
import org.kmb.eventhub.enums.EventFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private EventFormat format;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDate startDateTime;
    private LocalDate endDateTime;
    private Long organizerId;
}
