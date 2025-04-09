package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
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

import static org.kmb.eventhub.Tables.USER;

@Service
@AllArgsConstructor
public class FriendService {

    private final FriendRequestDao friendRequestDao;

    private final FriendRequestRepository friendRequestRepository;

    private final UserDao userDao;

    private static final String MEMBER_EXPECTED = "Ожидался тип пользователя MEMBER";

    @Transactional
    public List<FriendRequest> getFriendRequestList(User user) {

        if (Objects.isNull(user))
            throw new UserNotFoundException(-1L);

        return friendRequestRepository.fetchOptionalBySenderIdOrRecipientId(user.getId());
    }

    @Transactional
    public void sendFriendRequest(User userFrom, String usernameTo) {

        Optional<User> optionalUserTo = userDao.fetchOptional(USER.USERNAME, usernameTo);
        if (optionalUserTo.isEmpty())
            throw new UserNotFoundException(-1L);

        User userTo = optionalUserTo.get();
        if (!userTo.getRole().equals(RoleType.MEMBER))
            throw new UnexpectedException(MEMBER_EXPECTED);

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(userFrom.getId());
        friendRequest.setRecipientId(userTo.getId());
        friendRequest.setStatus(FriendRequestStatusType.PENDING);
        friendRequestDao.insert(friendRequest);
    }

    @Transactional
    public void acceptFriendRequest(Long idFrom, User userTo) {

        Optional<User> optionalUserFrom = userDao.fetchOptionalById(idFrom);
        if (optionalUserFrom.isEmpty())
            throw new UserNotFoundException(idFrom);

        User userFrom = optionalUserFrom.get();
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

        Optional<User> optionalUserFrom = userDao.fetchOptionalById(idFrom);
        if (optionalUserFrom.isEmpty())
            throw new UserNotFoundException(idFrom);

        User userFrom = optionalUserFrom.get();
        if (!userFrom.getRole().equals(RoleType.MEMBER))
            throw new UnexpectedException(MEMBER_EXPECTED);

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(userFrom.getId());
        friendRequest.setRecipientId(userTo.getId());
        friendRequest.setStatus(FriendRequestStatusType.REJECTED);
        friendRequestDao.insert(friendRequest);
    }
}
