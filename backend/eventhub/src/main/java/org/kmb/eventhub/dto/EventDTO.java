package org.kmb.eventhub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.kmb.eventhub.enums.EventFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private EventFormat format;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime startDateTime;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime endDateTime;
    private Long organizerId;
}
