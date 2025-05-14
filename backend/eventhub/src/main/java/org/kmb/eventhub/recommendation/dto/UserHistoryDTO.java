package org.kmb.eventhub.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserHistoryDTO {
    private Long id;
    private Long userId;
    private Long eventId;
    private String actionType;
    private String actionValue;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime actionTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
