package org.kmb.eventhub.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.kmb.eventhub.recommendation.enums.InteractionType;

import java.time.LocalDateTime;

@Data
public class UserEventInteractionDTO {
    private Long id;
    private Long userId;
    private Long eventId;
    private InteractionType interactionType;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime createdAt;
}
