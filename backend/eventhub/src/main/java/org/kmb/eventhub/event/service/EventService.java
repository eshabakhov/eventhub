package org.kmb.eventhub.event.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.auth.service.UserDetailsService;
import org.kmb.eventhub.event.dto.EventDTO;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.event.enums.EventFormat;
import org.kmb.eventhub.enums.FormatType;
import org.kmb.eventhub.common.exception.AlreadyExistsException;
import org.kmb.eventhub.event.exception.EventNotFoundException;
import org.kmb.eventhub.common.exception.MissingFieldException;
import org.kmb.eventhub.event.mapper.EventMapper;
import org.kmb.eventhub.event.mapper.EventFileMapper;
import org.kmb.eventhub.tag.mapper.TagMapper;
import org.kmb.eventhub.event.repository.EventRepository;
import org.kmb.eventhub.subscribe.repository.SubscribeRepository;
import org.kmb.eventhub.tag.repository.TagRepository;
import org.kmb.eventhub.tables.daos.*;
import org.kmb.eventhub.tables.pojos.Event;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.trueCondition;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private final TagRepository tagRepository;

    private final EventDao eventDao;

    private final EventMapper eventMapper;

    private final TagMapper tagMapper;

    private final MapService mapService;

    private final EventFileDao eventFileDao;

    private final EventFileMapper eventFileMapper;

    private final SubscribeRepository subscribeRepository;

    private final EventSecurityService eventSecurityService;

    private final UserDetailsService userDetailsService;

    private Condition getCommonListCondition(String search, List<String> tags) {
        Condition condition = trueCondition();

        if (Objects.nonNull(search) && !search.trim().isEmpty()) {
            condition = condition.and(org.kmb.eventhub.tables.Event.EVENT.TITLE.containsIgnoreCase(search));
            condition = condition.or(org.kmb.eventhub.tables.Event.EVENT.SHORT_DESCRIPTION.containsIgnoreCase(search));
            condition = condition.or(org.kmb.eventhub.tables.Event.EVENT.LOCATION.containsIgnoreCase(search));


            Map<String, String> formatRuMap = Map.of(
                    "ONLINE", "Онлайн",
                    "OFFLINE", "Офлайн"
            );

            List<EventFormat> matchingFormats = formatRuMap.entrySet().stream()
                    .filter(entry -> entry.getValue().toLowerCase().contains(search.toLowerCase()))
                    .map(entry -> EventFormat.valueOf(entry.getKey()))
                    .toList();

            if (!matchingFormats.isEmpty()) {
                condition = condition.or(org.kmb.eventhub.tables.Event.EVENT.FORMAT.in(matchingFormats));
            }

        }
        if (Objects.nonNull(tags) && !tags.isEmpty()) {
            condition = condition.and(org.kmb.eventhub.tables.Event.EVENT.ID.in(eventRepository.fetchEventIdsBySelectedTags(tags)));
        }

        return condition;
    }

    private ResponseList<EventDTO> getCommonList(Condition condition, Integer page, Integer pageSize) {
        ResponseList<EventDTO> responseList = new ResponseList<>();
        List<Event> eventList = eventRepository.fetch(condition, page, pageSize);
        List<EventDTO> eventDTOList = new ArrayList<>();
        eventList.forEach(event -> {
            EventDTO eventDTO = eventMapper.toDto(event);
            eventDTO.setTags(tagRepository.fetch(event.getId()).stream().map(tagMapper::toDto).collect(Collectors.toSet()));
            eventDTOList.add(eventDTO);
        });

        responseList.setList(eventDTOList);
        responseList.setTotal(eventRepository.count(condition));
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    public ResponseList<EventDTO> getList(Integer page, Integer pageSize, String search, List<String> tags) {
        Condition condition = getCommonListCondition(search, tags);
        return getCommonList(condition, page, pageSize);
    }

    public ResponseList<EventDTO> getListByOrganizerId(Integer page, Integer pageSize, String search, List<String> tags, Long orgId) {
        Condition condition = getCommonListCondition(search, tags);
        if (Objects.nonNull(orgId)) {
            condition = condition.and(org.kmb.eventhub.tables.Event.EVENT.ORGANIZER_ID.eq(orgId));
        }
        return getCommonList(condition, page, pageSize);
    }

    public ResponseList<EventDTO> getListByMemberId(Integer page, Integer pageSize, String search, List<String> tags, Long memberId) {
        Condition condition = getCommonListCondition(search, tags);
        if (Objects.nonNull(memberId)) {
            condition = condition.and(org.kmb.eventhub.tables.Event.EVENT.ID.in(subscribeRepository.fetchEventsIDsByMemberId(memberId)));
        }
        return getCommonList(condition, page, pageSize);
    }


    @Transactional
    public Event create(EventDTO eventDTO) {
        if (Objects.isNull(eventDTO.getTitle()) || eventDTO.getTitle().isEmpty())
            throw new MissingFieldException("title");

        if (Objects.isNull(eventDTO.getFormat()))
            throw new MissingFieldException("format");

        if (Objects.isNull(eventDTO.getShortDescription()))
            throw new MissingFieldException("shortDescription");

        if (Objects.isNull(eventDTO.getStartDateTime()))
            throw new MissingFieldException("startDateTime");

        if (Objects.isNull(eventDTO.getEndDateTime()))
            throw new MissingFieldException("endDateTime");

        if (Objects.isNull(eventDTO.getOrganizerId()))
            throw new MissingFieldException("organizerId");
        if (eventDTO.getStartDateTime().isAfter(eventDTO.getEndDateTime()))
            throw new IllegalArgumentException("startDateTime > endDateTime");

        if ((Objects.isNull(eventDTO.getLocation()) || eventDTO.getLocation().isEmpty())
                && Objects.nonNull(eventDTO.getLatitude()) && Objects.nonNull(eventDTO.getLongitude()))
            eventDTO.setLocation(mapService.getAddress(eventDTO.getLatitude(), eventDTO.getLongitude(), Objects.equals(eventDTO.getFormat(), EventFormat.ONLINE)));

        if (Objects.nonNull(eventDTO.getLocation()) && !eventDTO.getLocation().isEmpty()
                && Objects.isNull(eventDTO.getLatitude()) && Objects.isNull(eventDTO.getLongitude())) {
            var coordinates = mapService.getCoordinates(eventDTO.getLocation(), eventDTO.getFormat() == EventFormat.ONLINE);
            eventDTO.setLatitude(coordinates.getLatitude());
            eventDTO.setLongitude(coordinates.getLongitude());
        }

        Event event = eventMapper.dtoToEvent(eventDTO);
        if (!eventDao.fetchByTitle(eventDTO.getTitle()).isEmpty()) {
            throw new AlreadyExistsException(String.format("Event with title %s", eventDTO.getTitle()));
        }
        eventDao.insert(event);

        if (Objects.nonNull(eventDTO.getFiles())) {
            eventDTO.getFiles().forEach(file -> file.setEventId(event.getId()));
            eventFileDao.insert(eventDTO.getFiles().stream().map(eventFileMapper::toEntity).toList());
        }
        return event;
    }

    public EventDTO get(Long id) {
        EventDTO eventDTO = eventMapper.toDto(eventDao.fetchOptionalById(id)
                .orElseThrow(() -> new EventNotFoundException(id)));
        eventDTO.setFiles(eventFileDao.fetchByEventId(id).stream().map(eventFileMapper::toDto).collect(Collectors.toSet()));
        eventDTO.setTags(tagRepository.fetch(id).stream().map(tagMapper::toDto).collect(Collectors.toSet()));
        return eventDTO;
    }

    @Transactional
    public Event update(Long id, EventDTO eventDTO) {
        Event event = eventDao.findOptionalById(id).orElseThrow(() -> new EventNotFoundException(id));

        if (Objects.nonNull(eventDTO.getDescription()))
            event.setDescription(eventDTO.getDescription());

        if (Objects.nonNull(eventDTO.getShortDescription()))
            event.setDescription(eventDTO.getShortDescription());

        if (Objects.nonNull(eventDTO.getTitle()))
            event.setTitle(eventDTO.getTitle());

        if (Objects.nonNull(eventDTO.getFormat()))
            event.setFormat(FormatType.valueOf(String.valueOf(eventDTO.getFormat())));

        if (Objects.nonNull(eventDTO.getStartDateTime()))
            event.setStartDateTime(eventDTO.getStartDateTime());

        if (Objects.nonNull(eventDTO.getEndDateTime()))
            event.setEndDateTime(eventDTO.getEndDateTime());

        // TODO
        // Нужно подумать проверять ли соответствие адреса координатам и что первичнее - адрес или координаты,
        // если на вход придет московский адрес и координаты Дагестана
        if (Objects.nonNull(eventDTO.getLocation()))
            event.setLocation(eventDTO.getLocation());

        if (Objects.nonNull(eventDTO.getLatitude()))
            event.setLatitude(eventDTO.getLatitude());

        if (Objects.nonNull(eventDTO.getLongitude()))
            event.setLongitude(eventDTO.getLongitude());

        event.setId(id);
        eventDao.update(event);
        return event;
    }
    @Transactional
    public Long delete(Long eventId) {
        if (eventSecurityService.isUserOwnEvent(eventId, userDetailsService.getAuthenticatedUser())) {
            eventDao.findOptionalById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
            eventDao.deleteById(eventId);
        }

        return eventId;
    }

    public List<EventDTO> getAll() {
        List<Event> eventList = eventRepository.findAll();
        List<EventDTO> eventDTOList = new ArrayList<>();
        eventList.forEach(event -> {
            EventDTO eventDTO = eventMapper.toDto(event);
            eventDTO.setTags(tagRepository.fetch(event.getId()).stream().map(tagMapper::toDto).collect(Collectors.toSet()));
            eventDTOList.add(eventDTO);
        });

        return eventDTOList;
    }
}
