package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.jooq.*;
import org.jooq.exception.DataTypeException;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.enums.FriendRequestStatusType;
import org.kmb.eventhub.enums.RoleType;
import org.kmb.eventhub.exception.FriendRequestException;
import org.kmb.eventhub.exception.UnexpectedException;
import org.kmb.eventhub.exception.UserNotFoundException;
import org.kmb.eventhub.exception.UserSelfException;
import org.kmb.eventhub.repository.FriendRequestRepository;
import org.kmb.eventhub.tables.daos.FriendRequestDao;
import org.kmb.eventhub.tables.pojos.FriendRequest;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public ResponseList<FriendRequest> getFriendRequestList(Long id, Integer page, Integer pageSize) {
        if (Objects.isNull(userService.get(id)))
            throw new UserNotFoundException(id);

        ResponseList<FriendRequest> responseList = new ResponseList<>();
        Condition condition = trueCondition();

        List<FriendRequest> list =  friendRequestRepository.fetchOptionalBySenderIdOrRecipientId(id, id, page, pageSize);

        responseList.setList(list);
        responseList.setTotal(friendRequestRepository.count(condition));
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
            throw new UnexpectedException(MEMBER_EXPECTED);
        List<FriendRequest> firstList = friendRequestRepository.fetchOptionalBySenderIdAndRecipientId(userIdFrom, userIdTo, 1, 10);
        List<FriendRequest> secondList = friendRequestRepository.fetchOptionalBySenderIdAndRecipientId(userIdTo, userIdFrom, 1, 10);
        if (firstList.size() > 1 || secondList.size() > 1)
            throw new UnexpectedException("More than one relation between users found");
        firstList.forEach(e -> {
            if (e.getStatus() == FriendRequestStatusType.ACCEPTED)
                throw new FriendRequestException(String.format("User with id %d and the user with id %d are friends", userIdFrom, userTo.getId()));
            if (e.getStatus() == FriendRequestStatusType.PENDING)
                throw new FriendRequestException(String.format("The user with id %d sent request to the user with id %d", userIdFrom, userTo.getId()));
        });
        secondList.forEach(e -> {
            if (e.getStatus() == FriendRequestStatusType.ACCEPTED)
                throw new FriendRequestException(String.format("User with id %d and the user with id %d are friends", userTo.getId(), userIdFrom));
            if (e.getStatus() == FriendRequestStatusType.PENDING)
                throw new FriendRequestException(String.format("The user with id %d sent request to the user with id %d", userTo.getId(), userIdFrom));
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
            throw new UnexpectedException(MEMBER_EXPECTED);
        List<FriendRequest> firstList = friendRequestRepository.fetchOptionalBySenderIdAndRecipientId(userIdFrom, userIdTo, 1, 10);
        List<FriendRequest> secondList = friendRequestRepository.fetchOptionalBySenderIdAndRecipientId(userIdTo, userIdFrom, 1, 10);
        if (firstList.isEmpty())
            throw new UnexpectedException(String.format("User with id %d did not send a request to user with id %d", userIdFrom, userTo.getId()));
        if (firstList.size() > 1 || secondList.size() > 1)
            throw new UnexpectedException("More than one relation between users found");
        firstList.forEach(e -> {
            if (e.getStatus() == FriendRequestStatusType.ACCEPTED)
                throw new FriendRequestException(String.format("User with id %d and the user with id %d are friends", userIdFrom, userIdTo));
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
            throw new UnexpectedException(MEMBER_EXPECTED);
        List<FriendRequest> firstList = friendRequestRepository.fetchOptionalBySenderIdAndRecipientId(userIdFrom, userIdTo, 1, 10);
        List<FriendRequest> secondList = friendRequestRepository.fetchOptionalBySenderIdAndRecipientId(userIdTo, userIdFrom, 1, 10);
        if (firstList.isEmpty())
            throw new UnexpectedException(String.format("User with id %d did not send a request to user with id %d", userIdFrom, userTo.getId()));
        if (firstList.size() > 1 || secondList.size() > 1)
            throw new UnexpectedException("More than one relation between users found");
        firstList.forEach(e -> {
            if (e.getStatus() == FriendRequestStatusType.ACCEPTED)
                throw new FriendRequestException(String.format("User with id %d and the user with id %d are friends", userIdFrom, userIdTo));
            /*if (e.getStatus() == FriendRequestStatusType.REJECTED)
                throw new FriendRequestException(String.format("User with id %d rejected the user with id %d", userIdTo, userIdFrom));*/
        });

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(userIdFrom);
        friendRequest.setRecipientId(userTo.getId());
        //friendRequest.setStatus(FriendRequestStatusType.REJECTED);
        friendRequestDao.update(friendRequest);

        //friendRequestDao.deleteById(userIdFrom);
    }
}
