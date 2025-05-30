package org.kmb.eventhub.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import org.kmb.eventhub.tag.dto.TagDTO;
import org.kmb.eventhub.event.enums.EventFormat;

@Data
public class EventDTO {
    private Long id;
    private String title;
    private String shortDescription;
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
    private Set<EventFileDTO> files;
    private Set<TagDTO> tags;
    private byte[] pictures;
    private Long views;
    private Long subscribers;
}
