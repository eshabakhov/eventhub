package org.kmb.eventhub.friendship.service;

import lombok.AllArgsConstructor;
import org.jooq.*;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.friendship.repository.FriendRequestRepository;
import org.kmb.eventhub.friendship.dto.FriendRequestDTO;
import org.kmb.eventhub.friendship.dto.RequesterDTO;
import org.kmb.eventhub.friendship.enums.FriendRequestStatusEnum;
import org.kmb.eventhub.enums.FriendRequestStatusType;
import org.kmb.eventhub.enums.RoleType;
import org.kmb.eventhub.friendship.exception.FriendRequestException;
import org.kmb.eventhub.user.exception.UserNotFoundException;
import org.kmb.eventhub.user.exception.UserSelfException;
import org.kmb.eventhub.tables.daos.FriendRequestDao;
import org.kmb.eventhub.tables.pojos.FriendRequest;
import org.kmb.eventhub.tables.pojos.User;
import org.kmb.eventhub.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.jooq.impl.DSL.trueCondition;

@Service
@AllArgsConstructor
public class FriendService {

    private final FriendRequestDao friendRequestDao;

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
    }
}
