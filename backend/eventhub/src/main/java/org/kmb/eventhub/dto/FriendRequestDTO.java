package org.kmb.eventhub.dto;

import lombok.Data;
import org.kmb.eventhub.enums.FriendRequestStatusEnum;

@Data
public class FriendRequestDTO {
    private Long senderId;
    private Long recipientId;
    private FriendRequestStatusEnum friendRequestStatus;
}

