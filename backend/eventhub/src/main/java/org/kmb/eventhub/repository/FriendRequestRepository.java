package org.kmb.eventhub.repository;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.kmb.eventhub.tables.pojos.FriendRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.kmb.eventhub.Tables.FRIEND_REQUEST;

@Repository
@AllArgsConstructor
public class FriendRequestRepository {

    private final DSLContext dslContext;

    public List<FriendRequest> fetchOptionalBySenderIdOrRecipientId(Long idFrom, Long idTo, Integer page, Integer pageSize) {
        return dslContext
                .selectFrom(FRIEND_REQUEST)
                .where(FRIEND_REQUEST.SENDER_ID.eq(idFrom))
                .or(FRIEND_REQUEST.RECIPIENT_ID.eq(idTo))
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(FriendRequest.class);
    }

    public List<FriendRequest> fetchOptionalBySenderIdAndRecipientId(Long idFrom, Long idTo, Integer page, Integer pageSize) {
        return dslContext
                .selectFrom(FRIEND_REQUEST)
                .where(FRIEND_REQUEST.SENDER_ID.eq(idFrom))
                .and(FRIEND_REQUEST.RECIPIENT_ID.eq(idTo))
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(FriendRequest.class);
    }

    public void deleteFriendRequestByIds(Long idFrom, Long idTo) {
        dslContext
            .deleteFrom(FRIEND_REQUEST)
            .where(FRIEND_REQUEST.SENDER_ID.eq(idFrom))
            .and(FRIEND_REQUEST.RECIPIENT_ID.eq(idTo))
            .execute();
    }

    public Long count(Condition condition) {
        return dslContext
                .selectCount()
                .from(FRIEND_REQUEST)
                .where(condition)
                .fetchOneInto(Long.class);
    }
}
