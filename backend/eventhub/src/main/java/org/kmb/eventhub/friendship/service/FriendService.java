package org.kmb.eventhub.friendship.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.kmb.eventhub.auth.service.UserDetailsService;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.enums.PrivacyType;
import org.kmb.eventhub.friendship.dto.FriendCheckDTO;
import org.kmb.eventhub.enums.FriendRequestStatusType;
import org.kmb.eventhub.enums.RoleType;
import org.kmb.eventhub.friendship.repository.FriendRequestRepository;
import org.kmb.eventhub.tables.FriendRequest;
import org.kmb.eventhub.tables.User;
import org.kmb.eventhub.tables.daos.MemberDao;
import org.kmb.eventhub.tables.pojos.Member;
import org.kmb.eventhub.user.dto.UserDTO;
import org.kmb.eventhub.user.dto.UserResponseDTO;
import org.kmb.eventhub.user.enums.PrivacyEnum;
import org.kmb.eventhub.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static org.jooq.impl.DSL.trueCondition;
import static org.kmb.eventhub.Tables.*;

@Service
@AllArgsConstructor
public class FriendService {

    private final UserRepository userRepository;

    private final FriendRequestRepository friendRequestRepository;

    private final MemberDao memberDao;

    private final UserDetailsService userDetailsService;

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

        boolean exists = friendRequestRepository.fetchExists(senderId, receiverId);

        if (exists) {
            throw new IllegalStateException("Запрос уже существует");
        }

        friendRequestRepository.insertPendingRequest(senderId, receiverId);
    }

    public void acceptRequest(Long senderId, Long currentUserId) {
        int updated = friendRequestRepository.updateAcceptRequest(senderId, currentUserId);
        if (updated == 0) {
            throw new IllegalStateException("Запрос не найден или уже обработан");
        }
    }

    public void rejectRequest(Long senderId, Long currentUserId) {
        int updated = friendRequestRepository.updateRejectRequest(senderId, currentUserId);
        if (updated == 0) {
            throw new IllegalStateException("Запрос не найден или уже обработан");
        }
    }

    public void removeFriend(Long senderId, Long currentUserId) {

        int updated = friendRequestRepository.removeFriend(senderId, currentUserId);

        if (updated == 0) {
            throw new IllegalStateException("Запрос не найден или уже обработан");
        }
    }

    public FriendCheckDTO isFriend(Long senderId, Long currentUserId) {
        FriendCheckDTO friendCheckDTO = new FriendCheckDTO();
        Long count = friendRequestRepository.isFriend(senderId, currentUserId);

        Member member = memberDao.fetchOneById(senderId);

        if (count > 0L) {
            friendCheckDTO.setFriendly(true);
            return friendCheckDTO;
        }
        friendCheckDTO.setFriendly(false);
        if (member.getPrivacy().equals(PrivacyType.ONLY_FRIENDS))
            friendCheckDTO.setPrivacy(PrivacyEnum.ONLY_FRIENDS);
        if (member.getPrivacy().equals(PrivacyType.PRIVATE))
            friendCheckDTO.setPrivacy(PrivacyEnum.PRIVATE);
        if (member.getPrivacy().equals(PrivacyType.PUBLIC))
            friendCheckDTO.setPrivacy(PrivacyEnum.PUBLIC);
        return friendCheckDTO;
    }

    public ResponseList<UserDTO> getFriends(Integer page, Integer pageSize, Long userId) {
        Long count = friendRequestRepository.getCountFriends(userId);
        List<UserDTO> list = friendRequestRepository.getFriendList(page, pageSize, userId);

        ResponseList<UserDTO> responseList = new ResponseList<>();
        responseList.setList(list);
        responseList.setTotal(count);
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    public List<UserDTO> getNonFriends(Long userId) {
        return friendRequestRepository.getNonFriends(userId);
    }

    public List<UserDTO> getIncomingRequests(Long userId) {
        return friendRequestRepository.getIncomingRequests(userId);
    }

    public List<UserDTO> getOutgoingRequests(Long userId) {
        return friendRequestRepository.getOutgoingRequests(userId);
    }
}
