package org.kmb.eventhub.dto;

import lombok.Data;
import java.time.LocalDate;
import org.kmb.eventhub.enums.FriendRequestStatusEnum;

@Data
public class FriendRequest {
    private Long requesterId;
    private Long addresseeId;
    private FriendRequestStatusEnum status;
    private LocalDate createdAt;
}

