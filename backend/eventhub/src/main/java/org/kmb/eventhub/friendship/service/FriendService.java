package org.kmb.eventhub.friendship.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.kmb.eventhub.auth.service.UserDetailsService;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.friendship.dto.FriendCheckDTO;
import org.kmb.eventhub.friendship.repository.FriendRequestRepository;
import org.kmb.eventhub.friendship.dto.FriendRequestDTO;
import org.kmb.eventhub.friendship.dto.RequesterDTO;
import org.kmb.eventhub.friendship.enums.FriendRequestStatusEnum;
import org.kmb.eventhub.enums.FriendRequestStatusType;
import org.kmb.eventhub.enums.RoleType;
import org.kmb.eventhub.friendship.exception.FriendRequestException;
import org.kmb.eventhub.tables.FriendRequest;
import org.kmb.eventhub.tables.User;
import org.kmb.eventhub.tables.records.FriendRequestRecord;
import org.kmb.eventhub.user.dto.UserDTO;
import org.kmb.eventhub.user.dto.UserResponseDTO;
import org.kmb.eventhub.user.exception.UserNotFoundException;
import org.kmb.eventhub.user.exception.UserSelfException;
import org.kmb.eventhub.tables.daos.FriendRequestDao;
import org.kmb.eventhub.user.repository.UserRepository;
import org.kmb.eventhub.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.jooq.impl.DSL.trueCondition;
import static org.kmb.eventhub.Tables.*;

@Service
@AllArgsConstructor
public class FriendService {

    /*private final FriendRequestDao friendRequestDao;

    private final FriendRequestRepository friendRequestRepository;

    private final UserService userService;

    private static final String MEMBER_EXPECTED = "User role expected MEMBER";
    private static final String RELATIONS_NOT_FOUND = "Relations between users not found";
    private static final String MORE_THAN_ONE_RELATION = "More than one relation between users found";
    private static final String FRIEND_REQUEST_SENT = "User with id %d sent request to user with id %d";
    private static final String USERS_ARE_FRIENDS = "User with id %d and the user with id %d are friends";

    public ResponseList<FriendRequestDTO> getFriendRequestList(Long id, Integer page, Integer pageSize) {

        User user1 = userService.get(id);
        if (Objects.isNull(user1))
            throw new UserNotFoundException(id);

        ResponseList<FriendRequestDTO> responseList = new ResponseList<>();
        Condition condition = trueCondition();

        List<FriendRequest> list =  friendRequestRepository.fetchOptionalBySenderIdOrRecipientId(id, id, page, pageSize);

        List<FriendRequestDTO> friendRequestDTOList = new ArrayList<>();

        list.forEach(e -> {
            User user2 = null;
            if (!Objects.equals(user1.getId(), e.getSenderId())) {
                user2 = userService.get(e.getSenderId());
            }
            if (Objects.equals(user1.getId(), e.getSenderId())) {
                user2 = userService.get(e.getRecipientId());
            }

            FriendRequestDTO friendRequestDTO = new FriendRequestDTO();
            RequesterDTO sender = new RequesterDTO();
            RequesterDTO recipient = new RequesterDTO();
            friendRequestDTO.setSender(sender);
            friendRequestDTO.setRecipient(recipient);
            if (Objects.equals(user1.getId(), e.getSenderId())) {
                sender.setId(user1.getId());
                sender.setUsername(user1.getUsername());
                sender.setDisplayName(user1.getDisplayName());
                recipient.setId(user2.getId());
                recipient.setUsername(user2.getUsername());
                recipient.setDisplayName(user2.getDisplayName());
            }
            if (Objects.equals(user2.getId(), e.getSenderId())) {
                sender.setId(user2.getId());
                sender.setUsername(user2.getUsername());
                sender.setDisplayName(user2.getDisplayName());
                recipient.setId(user1.getId());
                recipient.setUsername(user1.getUsername());
                recipient.setDisplayName(user1.getDisplayName());
            }
            if (FriendRequestStatusType.PENDING.equals(e.getStatus()))
                friendRequestDTO.setFriendRequestStatus(FriendRequestStatusEnum.PENDING);
            if (FriendRequestStatusType.ACCEPTED.equals(e.getStatus()))
                friendRequestDTO.setFriendRequestStatus(FriendRequestStatusEnum.ACCEPTED);

            friendRequestDTOList.add(friendRequestDTO);
        });

        responseList.setList(friendRequestDTOList);
        responseList.setTotal((long) friendRequestDTOList.size());
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    @Transactional
    public void sendFriendRequest(Long userIdFrom, Long userIdTo) {

        if (Objects.equals(userIdFrom, userIdTo))
            throw new UserSelfException(userIdFrom);

        userService.get(userIdFrom);
        User userTo = userService.get(userIdTo);

        if (!userTo.getRole().equals(RoleType.MEMBER))
            throw new FriendRequestException(MEMBER_EXPECTED);
        List<FriendRequest> firstList = friendRequestRepository.fetchOptionalBySenderIdAndRecipientId(userIdFrom, userIdTo, 1, 10);
        List<FriendRequest> secondList = friendRequestRepository.fetchOptionalBySenderIdAndRecipientId(userIdTo, userIdFrom, 1, 10);
        if (!secondList.isEmpty())
            throw new FriendRequestException(String.format(FRIEND_REQUEST_SENT, userIdTo, userIdFrom));
        if (firstList.size() > 1)
            throw new FriendRequestException(MORE_THAN_ONE_RELATION);
        firstList.forEach(e -> {
            if (e.getStatus() == FriendRequestStatusType.ACCEPTED)
                throw new FriendRequestException(String.format(USERS_ARE_FRIENDS, userIdFrom, userIdTo));
            if (e.getStatus() == FriendRequestStatusType.PENDING)
                throw new FriendRequestException(String.format(FRIEND_REQUEST_SENT, userIdFrom, userIdTo));
        });

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(userIdFrom);
        friendRequest.setRecipientId(userTo.getId());
        friendRequest.setStatus(FriendRequestStatusType.PENDING);
        friendRequestDao.insert(friendRequest);
    }

    @Transactional
    public void acceptFriendRequest(Long userIdFrom, Long userIdTo) {

        if (Objects.equals(userIdFrom, userIdTo))
            throw new UserSelfException(userIdFrom);

        userService.get(userIdFrom);
        User userTo = userService.get(userIdTo);

        if (!userTo.getRole().equals(RoleType.MEMBER))
            throw new FriendRequestException(MEMBER_EXPECTED);
        List<FriendRequest> firstList = friendRequestRepository.fetchOptionalBySenderIdAndRecipientId(userIdFrom, userIdTo, 1, 10);
        List<FriendRequest> secondList = friendRequestRepository.fetchOptionalBySenderIdAndRecipientId(userIdTo, userIdFrom, 1, 10);
        if (firstList.isEmpty())
            throw new FriendRequestException(RELATIONS_NOT_FOUND);
        if (!secondList.isEmpty())
            secondList.forEach(e -> {
                if (e.getStatus() == FriendRequestStatusType.ACCEPTED)
                    throw new FriendRequestException(String.format(USERS_ARE_FRIENDS, userIdFrom, userIdTo));
                if (e.getStatus() == FriendRequestStatusType.PENDING)
                    throw new FriendRequestException(String.format(FRIEND_REQUEST_SENT, userIdFrom, userIdTo));
            });
        if (firstList.size() > 1)
            throw new FriendRequestException(MORE_THAN_ONE_RELATION);
        firstList.forEach(e -> {
            if (e.getStatus() == FriendRequestStatusType.ACCEPTED)
                throw new FriendRequestException(String.format(USERS_ARE_FRIENDS, userIdFrom, userIdTo));
        });

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(userIdFrom);
        friendRequest.setRecipientId(userTo.getId());
        friendRequest.setStatus(FriendRequestStatusType.ACCEPTED);
        friendRequestDao.update(friendRequest);
    }

    @Transactional
    public void rejectFriendRequest(Long userIdFrom, Long userIdTo) {

        if (Objects.equals(userIdFrom, userIdTo))
            throw new UserSelfException(userIdFrom);

        userService.get(userIdFrom);
        User userTo = userService.get(userIdTo);

        if (!userTo.getRole().equals(RoleType.MEMBER))
            throw new FriendRequestException(MEMBER_EXPECTED);
        List<FriendRequest> firstList = friendRequestRepository.fetchOptionalBySenderIdAndRecipientId(userIdFrom, userIdTo, 1, 10);
        List<FriendRequest> secondList = friendRequestRepository.fetchOptionalBySenderIdAndRecipientId(userIdTo, userIdFrom, 1, 10);
        if (firstList.isEmpty())
            throw new FriendRequestException(RELATIONS_NOT_FOUND);
        if (!secondList.isEmpty())
            secondList.forEach(e -> {
                if (e.getStatus() == FriendRequestStatusType.ACCEPTED)
                    throw new FriendRequestException(String.format(USERS_ARE_FRIENDS, userIdFrom, userIdTo));
                if (e.getStatus() == FriendRequestStatusType.PENDING)
                    throw new FriendRequestException(String.format(FRIEND_REQUEST_SENT, userIdFrom, userIdTo));
            });
        if (firstList.size() > 1)
            throw new FriendRequestException(MORE_THAN_ONE_RELATION);

        friendRequestRepository.deleteFriendRequestByIds(userIdFrom, userIdTo);
    }

    @Transactional
    public void removeUserFromFriends(Long userIdFrom, Long userIdTo) {

        if (Objects.equals(userIdFrom, userIdTo))
            throw new UserSelfException(userIdFrom);

        userService.get(userIdFrom);
        User userTo = userService.get(userIdTo);

        if (!userTo.getRole().equals(RoleType.MEMBER))
            throw new FriendRequestException(MEMBER_EXPECTED);
        List<FriendRequest> firstList = friendRequestRepository.fetchOptionalBySenderIdAndRecipientId(userIdFrom, userIdTo, 1, 10);
        List<FriendRequest> secondList = friendRequestRepository.fetchOptionalBySenderIdAndRecipientId(userIdTo, userIdFrom, 1, 10);
        if (firstList.isEmpty() && secondList.isEmpty())
            throw new FriendRequestException(RELATIONS_NOT_FOUND);
        if (firstList.size() > 1 || secondList.size() > 1)
            throw new FriendRequestException(MORE_THAN_ONE_RELATION);
        if (!firstList.isEmpty()) {
            firstList.forEach(e -> {
                if (e.getStatus() == FriendRequestStatusType.PENDING)
                    throw new FriendRequestException(String.format(FRIEND_REQUEST_SENT, userIdFrom, userIdTo));
            });
            friendRequestRepository.deleteFriendRequestByIds(userIdFrom, userIdTo);
        }
        if (!secondList.isEmpty()) {
            secondList.forEach(e -> {
                if (e.getStatus() == FriendRequestStatusType.PENDING)
                    throw new FriendRequestException(String.format(FRIEND_REQUEST_SENT, userIdFrom, userIdTo));
            });
            friendRequestRepository.deleteFriendRequestByIds(userIdTo, userIdFrom);
        }
    }*/

    private final UserRepository userRepository;

    private final UserDetailsService userDetailsService;

    private final DSLContext dsl;
    private final User U = USER;
    private final FriendRequest F = FRIEND_REQUEST;

    public ResponseList<UserResponseDTO> getMemberList(Integer page, Integer pageSize, String search) {

        ResponseList<UserResponseDTO> responseList = new ResponseList<>();

        Condition condition = trueCondition().and(USER.ROLE.eq(RoleType.MEMBER));

        if (Objects.nonNull(search) && !search.isEmpty()) {
            condition = condition.and(USER.USERNAME.eq(search));
        }

        List<UserResponseDTO> list =  userRepository.fetchWithoutCurrentUser(condition, page, pageSize, userDetailsService.getAuthenticatedUser().getId());

        responseList.setList(list);
        responseList.setTotal(userRepository.count(condition) - 1);
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    public void sendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Нельзя добавить себя");
        }

        boolean exists = dsl.fetchExists(
                dsl.selectFrom(F)
                        .where(F.SENDER_ID.eq(senderId))
                        .and(F.RECIPIENT_ID.eq(receiverId))
        );

        if (exists) {
            throw new IllegalStateException("Запрос уже существует");
        }

        dsl.insertInto(F)
                .set(F.SENDER_ID, senderId)
                .set(F.RECIPIENT_ID, receiverId)
                .set(F.STATUS, FriendRequestStatusType.PENDING)
                .execute();
    }

    public void acceptRequest(Long senderId, Long currentUserId) {
        int updated = dsl.update(F)
                .set(F.STATUS, FriendRequestStatusType.ACCEPTED)
                .where(F.SENDER_ID.eq(senderId))
                .and(F.RECIPIENT_ID.eq(currentUserId))
                .and(F.STATUS.eq(FriendRequestStatusType.PENDING))
                .execute();

        if (updated == 0) {
            throw new IllegalStateException("Запрос не найден или уже обработан");
        }
    }

    public void rejectRequest(Long senderId, Long currentUserId) {

        int updated = dsl.update(F)
                .set(F.STATUS, FriendRequestStatusType.REJECTED)
                .where(F.SENDER_ID.eq(senderId))
                .and(F.RECIPIENT_ID.eq(currentUserId))
                .and(F.STATUS.eq(FriendRequestStatusType.PENDING))
                .execute();

        if (updated == 0) {
            throw new IllegalStateException("Запрос не найден или уже обработан");
        }
    }

    public void removeFriend(Long senderId, Long currentUserId) {

        int updated = dsl.deleteFrom(FRIEND_REQUEST)
                .where(
                        (FRIEND_REQUEST.SENDER_ID.eq(senderId).and(FRIEND_REQUEST.RECIPIENT_ID.eq(currentUserId)))
                        .or(FRIEND_REQUEST.SENDER_ID.eq(currentUserId).and(FRIEND_REQUEST.RECIPIENT_ID.eq(senderId)))
                )
                .and(FRIEND_REQUEST.STATUS.eq(FriendRequestStatusType.ACCEPTED))
                .execute();

        if (updated == 0) {
            throw new IllegalStateException("Запрос не найден или уже обработан");
        }
    }

    public FriendCheckDTO isFriend(Long senderId, Long currentUserId) {
        FriendCheckDTO friendCheckDTO = new FriendCheckDTO();
        Long count = dsl.selectCount()
                .from(FRIEND_REQUEST)
                .where(
                        (FRIEND_REQUEST.SENDER_ID.eq(senderId).and(FRIEND_REQUEST.RECIPIENT_ID.eq(currentUserId)))
                        .or(FRIEND_REQUEST.SENDER_ID.eq(currentUserId).and(FRIEND_REQUEST.RECIPIENT_ID.eq(senderId)))
                )
                .and(FRIEND_REQUEST.STATUS.eq(FriendRequestStatusType.ACCEPTED))
                .fetchOneInto(Long.class);

        if (count > 0L) {
            friendCheckDTO.setFriendly(true);
            return friendCheckDTO;
        }
        friendCheckDTO.setFriendly(false);
        return friendCheckDTO;
    }

    public ResponseList<UserDTO> getFriends(Integer page, Integer pageSize, Long userId) {
        Long count = dsl.selectCount()
                .from(USER)
                .where(USER.ID.ne(userId))
                .andExists(
                        dsl.selectOne()
                                .from(FRIEND_REQUEST)
                                .where(FRIEND_REQUEST.STATUS.eq(FriendRequestStatusType.ACCEPTED))
                                .and(
                                        FRIEND_REQUEST.SENDER_ID.eq(userId).and(FRIEND_REQUEST.RECIPIENT_ID.eq(USER.ID))
                                                .or(FRIEND_REQUEST.SENDER_ID.eq(USER.ID).and(FRIEND_REQUEST.RECIPIENT_ID.eq(userId)))
                                )
                ).fetchOneInto(Long.class);
        List<UserDTO> list = dsl.selectFrom(USER)
                .where(USER.ID.ne(userId))
                .andExists(
                        dsl.selectOne()
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

        ResponseList<UserDTO> responseList = new ResponseList<>();
        responseList.setList(list);
        responseList.setTotal(count);
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    public List<UserDTO> getNonFriends(Long userId) {
        return dsl.selectFrom(USER)
                .where(USER.ID.ne(userId))
                .and(USER.ROLE.eq(RoleType.MEMBER))// исключаем самого себя
                .andNotExists(
                        dsl.selectOne()
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
        return dsl.select(U.fields())
                .from(F)
                .join(U).on(F.RECIPIENT_ID.eq(userId).and(U.ID.eq(F.SENDER_ID)))
                .where(F.STATUS.eq(FriendRequestStatusType.PENDING))
                .fetchInto(UserDTO.class);
    }

    public List<UserDTO> getOutgoingRequests(Long userId) {
        return dsl.select(U.fields())
                .from(F)
                .join(U).on(F.SENDER_ID.eq(userId).and(U.ID.eq(F.RECIPIENT_ID)))
                .where(F.STATUS.eq(FriendRequestStatusType.PENDING))
                .fetchInto(UserDTO.class);
    }
}
