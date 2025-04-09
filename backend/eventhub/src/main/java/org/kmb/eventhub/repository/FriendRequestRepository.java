package org.kmb.eventhub.repository;

import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.kmb.eventhub.tables.pojos.FriendRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.kmb.eventhub.Tables.FRIEND_REQUEST;

@Repository
@AllArgsConstructor
public class FriendRequestRepository {

    private final DSLContext dslContext;

    public List<FriendRequest> fetchOptionalBySenderIdOrRecipientId(Long id) {
        return dslContext
                .selectFrom(FRIEND_REQUEST)
                .where(FRIEND_REQUEST.SENDER_ID.eq(id))
                .or(FRIEND_REQUEST.RECIPIENT_ID.eq(id))
                .fetchInto(FriendRequest.class);
    }
}
