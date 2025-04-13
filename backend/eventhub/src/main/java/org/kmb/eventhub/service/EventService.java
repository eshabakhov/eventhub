package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.dto.EventDTO;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.dto.TagDTO;
import org.kmb.eventhub.enums.EventFormat;
import org.kmb.eventhub.enums.FormatType;
import org.kmb.eventhub.exception.AlreadyExistsException;
import org.kmb.eventhub.exception.EventNotFoundException;
import org.kmb.eventhub.exception.MissingFieldException;
import org.kmb.eventhub.exception.UserNotFoundException;
import org.kmb.eventhub.mapper.EventMapper;
import org.kmb.eventhub.mapper.EventFileMapper;
import org.kmb.eventhub.mapper.TagMapper;
import org.kmb.eventhub.repository.EventRepository;
import org.kmb.eventhub.repository.TagRepository;
import org.kmb.eventhub.tables.daos.*;
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tables.pojos.Tag;
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

    private final TagService tagService;

    private final EventDao eventDao;

    private final EventMapper eventMapper;

    private final TagMapper tagMapper;

    private final MapService mapService;

    private final EventFileDao eventFileDao;

    private final EventFileMapper eventFileMapper;

    private final OrganizerDao organizerDao;


    public ResponseList<EventDTO> getList(Integer page, Integer pageSize) {
        ResponseList<EventDTO> responseList = new ResponseList<>();
        Condition condition = trueCondition();

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

    @Transactional
    public Event create(EventDTO eventDTO) {
        if (Objects.isNull(eventDTO.getTitle()) || eventDTO.getTitle().isEmpty())
            throw new MissingFieldException("title");

        if (Objects.isNull(eventDTO.getFormat()))
            throw new MissingFieldException("format");

        if (Objects.isNull(eventDTO.getStartDateTime()))
            throw new MissingFieldException("startDateTime");

        if (Objects.isNull(eventDTO.getEndDateTime()))
            throw new MissingFieldException("endDateTime");

        if (Objects.isNull(eventDTO.getOrganizerId()))
            throw new MissingFieldException("organizerId");
        if (eventDTO.getStartDateTime().isAfter(eventDTO.getEndDateTime()))
            throw new IllegalArgumentException("startDateTime > endDateTime");
        organizerDao.findOptionalById(eventDTO.getOrganizerId()).orElseThrow(() -> new UserNotFoundException(eventDTO.getOrganizerId()));

        if ((Objects.isNull(eventDTO.getLocation()) || eventDTO.getLocation().isEmpty())
                && Objects.nonNull(eventDTO.getLatitude()) && Objects.nonNull(eventDTO.getLongitude()))
            eventDTO.setLocation(mapService.getAddress(eventDTO.getLatitude(), eventDTO.getLongitude(), Objects.equals(eventDTO.getFormat(), EventFormat.ONLINE)));

        if (Objects.nonNull(eventDTO.getLocation()) && !eventDTO.getLocation().isEmpty()
                && Objects.isNull(eventDTO.getLatitude()) && Objects.isNull(eventDTO.getLongitude())) {
            var coordinates = mapService.getCoordinates(eventDTO.getLocation());
            eventDTO.setLatitude(coordinates.getLatitude());
            eventDTO.setLongitude(coordinates.getLongitude());
        }

        Event event = eventMapper.dtoToEvent(eventDTO);
        if (!eventDao.fetchByTitle(eventDTO.getTitle()).isEmpty()) {
            throw new AlreadyExistsException(String.format("Event with title %s", eventDTO.getTitle()));
        }
        eventDao.insert(event);

        if (Objects.nonNull(eventDTO.getFiles())) {
            eventDTO.getFiles().forEach(file -> {
                file.setEventId(event.getId());
            });
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
    public Long delete(Long id) {
        eventDao.findOptionalById(id).orElseThrow(() -> new EventNotFoundException(id));
        eventDao.deleteById(id);
        return id;
    }

    @Transactional
    public List<Tag> addTagsToEvent(Long eventId, List<TagDTO> tagNamesDTO) {
        List<Tag> tagNames = tagNamesDTO.stream().map(tagMapper::toEntity).toList();
        eventDao.findOptionalById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        //1. Добавление новых тегов, получение id всех тегов из запроса
        List<Tag> tagWithId = tagService.checkAllTags(tagNames);

        //2. Получение списка использованных тегов для мероприятия
        Set<Long> usedTagIds = tagService.getUsedTagIdsForEvent(eventId);

        //3. Получение id неиспользованных тегов
        List<Tag> newTags = tagWithId.stream()
                .filter(tag -> !usedTagIds.contains(tag.getId()))
                .toList();

        //4. Добавление связи для новых тегов и мероприятия
        if (!newTags.isEmpty()) {
            tagService.assignTagsToEvent(eventId, newTags);
        }

        return newTags;
    }
}
