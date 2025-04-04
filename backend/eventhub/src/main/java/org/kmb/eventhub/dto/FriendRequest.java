package org.kmb.eventhub.dto;

import lombok.Data;
import org.kmb.eventhub.enums.FriendRequestStatusEnum;

import java.time.LocalDate;
@Data
public class FriendRequest {

    private Long requesterId;

    private Long addresseeId;

    private FriendRequestStatusEnum status;

    private LocalDate createdAt;
}

