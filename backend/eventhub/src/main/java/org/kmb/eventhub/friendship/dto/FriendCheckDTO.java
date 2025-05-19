package org.kmb.eventhub.friendship.dto;

import lombok.Data;
import org.kmb.eventhub.user.enums.PrivacyEnum;

@Data
public class FriendCheckDTO {
    private Boolean friendly;
    private PrivacyEnum privacy;
}
