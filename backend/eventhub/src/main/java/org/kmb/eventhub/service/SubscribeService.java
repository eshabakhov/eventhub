package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.exception.UnexpectedException;
import org.kmb.eventhub.mapper.UserMapper;
import org.kmb.eventhub.repository.SubscribeRepository;
import org.kmb.eventhub.tables.daos.EventMembersDao;
import org.kmb.eventhub.tables.pojos.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class SubscribeService {

    private final EventService eventService;

    private final UserSecurityService userSecurityService;

    private final UserService userService;

    private final UserMapper userMapper;

    private final SubscribeRepository subscribeRepository;

    private final EventMembersDao eventMembersDao;

    public void subscribeToEvent(Long eventId, Long memberId) {
        eventService.get(eventId);
        userMapper.toMemberDto(userService.getMember(memberId));

        if (Objects.nonNull(subscribeRepository.fetchOptionalByMemberIdAndEventId(memberId, eventId, 1, 1)))
            throw new UnexpectedException(String.format("User %d already subscribed to event %d", memberId, eventId));

        EventMembers eventMembers = new EventMembers();
        eventMembers.setEventId(eventId);
        eventMembers.setMemberId(memberId);
        eventMembersDao.insert(eventMembers);

    }
    public void unsubscribeFromEvent(Long eventId, Long memberId) {
        if (userSecurityService.isUserOwnData(memberId)) {
            EventMembers eventMembers = subscribeRepository.fetchOptionalByMemberIdAndEventId(memberId, eventId, 1, 1);
            eventMembersDao.delete(eventMembers);
        }
    }

    public ResponseList<Event> getEventsByMemberId(Long memberId, Integer page, Integer pageSize) {
        ResponseList<Event> responseList = new ResponseList<>();
        List<Event> list = subscribeRepository.fetchEventsByMemberId(memberId, page, pageSize);
        responseList.setList(list);
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    public ResponseList<Member> getMembersByEventId(Long eventId, Integer page, Integer pageSize) {
        ResponseList<Member> responseList = new ResponseList<>();
        List<Member> list = subscribeRepository.fetchMembersByEventId(eventId, page, pageSize);
        responseList.setList(list);
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }
}
