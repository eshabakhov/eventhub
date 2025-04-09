package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.enums.FriendRequestStatusType;
import org.kmb.eventhub.enums.RoleType;
import org.kmb.eventhub.exception.UnexpectedException;
import org.kmb.eventhub.exception.UserNotFoundException;
import org.kmb.eventhub.repository.FriendRequestRepository;
import org.kmb.eventhub.tables.daos.FriendRequestDao;
import org.kmb.eventhub.tables.daos.UserDao;
import org.kmb.eventhub.tables.pojos.FriendRequest;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.jooq.impl.DSL.trueCondition;
import static org.kmb.eventhub.Tables.USER;

@Service
@AllArgsConstructor
public class FriendService {

    private final FriendRequestDao friendRequestDao;

    private final FriendRequestRepository friendRequestRepository;

    private final UserService userService;

    private static final String MEMBER_EXPECTED = "Ожидался тип пользователя MEMBER";

    public ResponseList<FriendRequest> getFriendRequestList(Long id, Integer page, Integer pageSize) {
        if (Objects.isNull(userService.get(id)))
            throw new UserNotFoundException(id);

        ResponseList<FriendRequest> responseList = new ResponseList<>();
        Condition condition = trueCondition();

        List<FriendRequest> list =  friendRequestRepository.fetchOptionalBySenderIdOrRecipientId(id, page, pageSize);

        responseList.setList(list);
        responseList.setTotal(friendRequestRepository.count(condition));
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    @Transactional
    public void sendFriendRequest(Long userIdFrom, String usernameTo) {

        User userTo = userService.getByUsername(usernameTo);

        if (!userTo.getRole().equals(RoleType.MEMBER))
            throw new UnexpectedException(MEMBER_EXPECTED);

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(userIdFrom);
        friendRequest.setRecipientId(userTo.getId());
        friendRequest.setStatus(FriendRequestStatusType.PENDING);
        friendRequestDao.insert(friendRequest);
    }

    @Transactional
    public void acceptFriendRequest(Long idFrom, User userTo) {

        User userFrom = userService.get(idFrom);

        if (!userFrom.getRole().equals(RoleType.MEMBER))
            throw new UnexpectedException(MEMBER_EXPECTED);

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(userFrom.getId());
        friendRequest.setRecipientId(userTo.getId());
        friendRequest.setStatus(FriendRequestStatusType.ACCEPTED);
        friendRequestDao.insert(friendRequest);
    }

    @Transactional
    public void rejectFriendRequest(Long idFrom, User userTo) {

        User userFrom = userService.get(idFrom);

        if (!userFrom.getRole().equals(RoleType.MEMBER))
            throw new UnexpectedException(MEMBER_EXPECTED);

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(userFrom.getId());
        friendRequest.setRecipientId(userTo.getId());
        friendRequest.setStatus(FriendRequestStatusType.REJECTED);
        friendRequestDao.insert(friendRequest);
    }
}
