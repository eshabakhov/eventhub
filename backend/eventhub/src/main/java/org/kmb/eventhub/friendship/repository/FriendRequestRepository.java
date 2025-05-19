package org.kmb.eventhub.friendship.repository;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.enums.FriendRequestStatusType;
import org.kmb.eventhub.enums.PrivacyType;
import org.kmb.eventhub.enums.RoleType;
import org.kmb.eventhub.friendship.dto.FriendCheckDTO;
import org.kmb.eventhub.tables.pojos.Member;
import org.kmb.eventhub.user.dto.UserDTO;
import org.kmb.eventhub.user.enums.PrivacyEnum;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.kmb.eventhub.Tables.FRIEND_REQUEST;
import static org.kmb.eventhub.Tables.USER;

@Repository
@AllArgsConstructor
public class FriendRequestRepository {

    private final DSLContext dslContext;

    public boolean fetchExists(Long senderId, Long receiverId) {
        return dslContext.fetchExists(
                dslContext.selectFrom(FRIEND_REQUEST)
                        .where(FRIEND_REQUEST.SENDER_ID.eq(senderId))
                        .and(FRIEND_REQUEST.RECIPIENT_ID.eq(receiverId))
        );
    }

    public void insertPendingRequest(Long senderId, Long receiverId) {
        dslContext.insertInto(FRIEND_REQUEST)
                .set(FRIEND_REQUEST.SENDER_ID, senderId)
                .set(FRIEND_REQUEST.RECIPIENT_ID, receiverId)
                .set(FRIEND_REQUEST.STATUS, FriendRequestStatusType.PENDING)
                .execute();
    }

    public int updateAcceptRequest(Long senderId, Long currentUserId) {
        return dslContext.update(FRIEND_REQUEST)
                .set(FRIEND_REQUEST.STATUS, FriendRequestStatusType.ACCEPTED)
                .where(FRIEND_REQUEST.SENDER_ID.eq(senderId))
                .and(FRIEND_REQUEST.RECIPIENT_ID.eq(currentUserId))
                .and(FRIEND_REQUEST.STATUS.eq(FriendRequestStatusType.PENDING))
                .execute();
    }

    public int updateRejectRequest(Long senderId, Long currentUserId) {
        return dslContext.update(FRIEND_REQUEST)
                .set(FRIEND_REQUEST.STATUS, FriendRequestStatusType.REJECTED)
                .where(FRIEND_REQUEST.SENDER_ID.eq(senderId))
                .and(FRIEND_REQUEST.RECIPIENT_ID.eq(currentUserId))
                .and(FRIEND_REQUEST.STATUS.eq(FriendRequestStatusType.PENDING))
                .execute();
    }

    public int removeFriend(Long senderId, Long currentUserId) {
        return dslContext.deleteFrom(FRIEND_REQUEST)
                .where(
                        (FRIEND_REQUEST.SENDER_ID.eq(senderId).and(FRIEND_REQUEST.RECIPIENT_ID.eq(currentUserId)))
                                .or(FRIEND_REQUEST.SENDER_ID.eq(currentUserId).and(FRIEND_REQUEST.RECIPIENT_ID.eq(senderId)))
                )
                .and(FRIEND_REQUEST.STATUS.eq(FriendRequestStatusType.ACCEPTED))
                .execute();
    }

    public Long isFriend(Long senderId, Long currentUserId) {
        return dslContext.selectCount()
                .from(FRIEND_REQUEST)
                .where(
                        (FRIEND_REQUEST.SENDER_ID.eq(senderId).and(FRIEND_REQUEST.RECIPIENT_ID.eq(currentUserId)))
                                .or(FRIEND_REQUEST.SENDER_ID.eq(currentUserId).and(FRIEND_REQUEST.RECIPIENT_ID.eq(senderId)))
                )
                .and(FRIEND_REQUEST.STATUS.eq(FriendRequestStatusType.ACCEPTED))
                .fetchOneInto(Long.class);
    }

    public Long getCountFriends(Long userId) {
        return dslContext.selectCount()
                .from(USER)
                .where(USER.ID.ne(userId))
                .andExists(
                        dslContext.selectOne()
                                .from(FRIEND_REQUEST)
                                .where(FRIEND_REQUEST.STATUS.eq(FriendRequestStatusType.ACCEPTED))
                                .and(
                                        FRIEND_REQUEST.SENDER_ID.eq(userId).and(FRIEND_REQUEST.RECIPIENT_ID.eq(USER.ID))
                                                .or(FRIEND_REQUEST.SENDER_ID.eq(USER.ID).and(FRIEND_REQUEST.RECIPIENT_ID.eq(userId)))
                                )
                ).fetchOneInto(Long.class);
    }

    public List<UserDTO> getFriendList(Integer page, Integer pageSize, Long userId) {
        return dslContext.selectFrom(USER)
                .where(USER.ID.ne(userId))
                .andExists(
                        dslContext.selectOne()
                                .from(FRIEND_REQUEST)
                                .where(FRIEND_REQUEST.STATUS.eq(FriendRequestStatusType.ACCEPTED))
                                .and(
                                        FRIEND_REQUEST.SENDER_ID.eq(userId).and(FRIEND_REQUEST.RECIPIENT_ID.eq(USER.ID))
                                                .or(FRIEND_REQUEST.SENDER_ID.eq(USER.ID).and(FRIEND_REQUEST.RECIPIENT_ID.eq(userId)))
                                )
                )
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(UserDTO.class);

    }

    public List<UserDTO> getNonFriends(Long userId) {
        return dslContext.selectFrom(USER)
                .where(USER.ID.ne(userId))
                .and(USER.ROLE.eq(RoleType.MEMBER))// исключаем самого себя
                .andNotExists(
                        dslContext.selectOne()
                                .from(FRIEND_REQUEST)
                                .where(
                                        FRIEND_REQUEST.STATUS.in(FriendRequestStatusType.PENDING, FriendRequestStatusType.ACCEPTED)
                                                .and(
                                                        FRIEND_REQUEST.SENDER_ID.eq(userId).and(FRIEND_REQUEST.RECIPIENT_ID.eq(USER.ID))
                                                                .or(FRIEND_REQUEST.SENDER_ID.eq(USER.ID).and(FRIEND_REQUEST.RECIPIENT_ID.eq(userId)))
                                                )
                                )
                )
                .fetchInto(UserDTO.class);
    }

    public List<UserDTO> getIncomingRequests(Long userId) {
        return dslContext.select(USER.fields())
                .from(FRIEND_REQUEST)
                .join(USER).on(FRIEND_REQUEST.RECIPIENT_ID.eq(userId).and(USER.ID.eq(FRIEND_REQUEST.SENDER_ID)))
                .where(FRIEND_REQUEST.STATUS.eq(FriendRequestStatusType.PENDING))
                .fetchInto(UserDTO.class);
    }

    public List<UserDTO> getOutgoingRequests(Long userId) {
        return dslContext.select(USER.fields())
                .from(FRIEND_REQUEST)
                .join(USER).on(FRIEND_REQUEST.SENDER_ID.eq(userId).and(USER.ID.eq(FRIEND_REQUEST.RECIPIENT_ID)))
                .where(FRIEND_REQUEST.STATUS.eq(FriendRequestStatusType.PENDING))
                .fetchInto(UserDTO.class);
    }

    public Long count(Condition condition) {
        return dslContext
                .selectCount()
                .from(FRIEND_REQUEST)
                .where(condition)
                .fetchOneInto(Long.class);
    }
}
