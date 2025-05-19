package org.kmb.eventhub.friendship.dto;

import lombok.Data;
import org.kmb.eventhub.friendship.enums.FriendRequestStatusEnum;

@Data
public class FriendRequestDTO {
    private RequesterDTO sender;
    private RequesterDTO recipient;
    private FriendRequestStatusEnum friendRequestStatus;
}

