package org.kmb.eventhub.subscribe.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.auth.service.UserDetailsService;
import org.kmb.eventhub.event.exception.EventNotFoundException;
import org.kmb.eventhub.event.dto.EventDTO;
import org.kmb.eventhub.event.dto.EventMemberDTO;
import org.kmb.eventhub.subscribe.exception.EventMemberNotFound;
import org.kmb.eventhub.subscribe.exception.MemberNotFound;
import org.kmb.eventhub.subscribe.exception.MemberSubscribeException;
import org.kmb.eventhub.tables.daos.*;
import org.kmb.eventhub.user.dto.MemberDTO;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.event.service.EventService;
import org.kmb.eventhub.event.mapper.EventMapper;
import org.kmb.eventhub.event.mapper.EventMemberMapper;
import org.kmb.eventhub.user.dto.MemberOrganizerDTO;
import org.kmb.eventhub.user.mapper.UserMapper;
import org.kmb.eventhub.subscribe.repository.SubscribeRepository;
import org.kmb.eventhub.tables.pojos.*;
import org.kmb.eventhub.user.service.UserSecurityService;
import org.kmb.eventhub.user.service.UserService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class SubscribeService {

    private final EventService eventService;

    private final UserSecurityService userSecurityService;

    private final UserDetailsService userDetailsService;

    private final UserService userService;

    private final UserMapper userMapper;

    private final SubscribeRepository subscribeRepository;

    private final EventMembersDao eventMembersDao;

    private final MemberOrganizerDao memberOrganizerDao;

    private final EventDao eventDao;

    private final EventMapper eventMapper;
    private final EventMemberMapper eventMemberMapper;
    private final UserDao userDao;
    private final MemberDao memberDao;


    public void subscribeToEvent(Long eventId, Long memberId) {
        eventService.get(eventId);
        userMapper.toMemberDto(userService.getMember(memberId));

        if (Objects.nonNull(subscribeRepository.fetchOptionalByMemberIdAndEventId(memberId, eventId, 1, 1)))
            throw new MemberSubscribeException(String.format("User %d already subscribed to event %d", memberId, eventId));

        EventMembers eventMembers = new EventMembers();
        eventMembers.setEventId(eventId);
        eventMembers.setMemberId(memberId);
        eventMembersDao.insert(eventMembers);

    }

    public void unsubscribeFromEvent(Long eventId, Long memberId) {
        if (userSecurityService.isUserOwnData(memberId, userDetailsService.getAuthenticatedUser())) {
            EventMembers eventMembers = subscribeRepository.fetchOptionalByMemberIdAndEventId(memberId, eventId, 1, 1);
            eventMembersDao.delete(eventMembers);
        }
    }

    public void subscribeToOrganizer(Long organizerId, Long memberId) {
        MemberOrganizer memberOrganizer = new MemberOrganizer();
        memberOrganizer.setMemberId(memberId);
        memberOrganizer.setOrganizerId(organizerId);
        memberOrganizerDao.insert(memberOrganizer);
    }

    public void unsubscribeFromOrganizer(Long organizerId, Long memberId) {
        if (userSecurityService.isUserOwnData(memberId, userDetailsService.getAuthenticatedUser())) {
            MemberOrganizer memberOrganizer = subscribeRepository.fetchOptionalByMemberIdAndOrganizerId(memberId, organizerId, 1, 1);
            memberOrganizerDao.delete(memberOrganizer);
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
    public EventMemberDTO checkSubscription(Long eventId, Long memberId) {
        EventDTO eventDTO = eventMapper.toDto(eventDao.fetchOptionalById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId)));

        MemberDTO memberDTO = userMapper.toMemberDto(memberDao.fetchOptionalById(memberId)
                .orElseThrow(() -> new MemberNotFound(memberId)));

        EventMemberDTO eventMembersDTO = eventMemberMapper.toDto(subscribeRepository.fetchOptionalByMemberIdAndEventId(memberId, eventId, 1, 1));

        if (Objects.isNull(eventMembersDTO)) {
            throw new EventMemberNotFound(memberId, eventId);
        }

        eventMembersDTO.setUserId(memberId);

        return eventMembersDTO;

    }
}
