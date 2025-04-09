package org.kmb.eventhub.repository;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.kmb.eventhub.tables.pojos.FriendRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.kmb.eventhub.Tables.FRIEND_REQUEST;
import static org.kmb.eventhub.Tables.USER;

@Repository
@AllArgsConstructor
public class FriendRequestRepository {

    private final DSLContext dslContext;

    public List<FriendRequest> fetchOptionalBySenderIdOrRecipientId(Long id, Integer page, Integer pageSize) {
        return dslContext
                .selectFrom(FRIEND_REQUEST)
                .where(FRIEND_REQUEST.SENDER_ID.eq(id))
                .or(FRIEND_REQUEST.RECIPIENT_ID.eq(id))
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(FriendRequest.class);
    }

    public Long count(Condition condition) {
        return dslContext
                .selectCount()
                .from(FRIEND_REQUEST)
                .where(condition)
                .fetchOneInto(Long.class);
    }
}
