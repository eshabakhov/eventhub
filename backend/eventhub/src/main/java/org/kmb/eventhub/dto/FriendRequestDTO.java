package org.kmb.eventhub.dto;

import lombok.Data;
import org.kmb.eventhub.enums.FriendRequestStatusEnum;

@Data
public class FriendRequestDTO {
    private RequesterDTO sender;
    private RequesterDTO recipient;
    private FriendRequestStatusEnum friendRequestStatus;
}

