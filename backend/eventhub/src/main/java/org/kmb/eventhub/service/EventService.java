package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.dto.EventDTO;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.dto.TagDTO;
import org.kmb.eventhub.enums.FormatType;
import org.kmb.eventhub.exception.EventNotFoundException;
import org.kmb.eventhub.exception.UserNotFoundException;
import org.kmb.eventhub.mapper.EventMapper;
import org.kmb.eventhub.mapper.EventFileMapper;
import org.kmb.eventhub.mapper.TagMapper;
import org.kmb.eventhub.repository.EventRepository;
import org.kmb.eventhub.tables.daos.EventDao;
import org.kmb.eventhub.tables.daos.EventFileDao;
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tables.pojos.EventFile;
import org.kmb.eventhub.tables.pojos.Tag;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.trueCondition;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private final TagService tagService;

    private final EventDao eventDao;

    private final EventMapper eventMapper;

    private final TagMapper tagMapper;

    private final MapService mapService;

    private final EventFileDao eventFileDao;

    private final EventFileMapper eventFileMapper;

    public ResponseList<Event> getList(Integer page, Integer pageSize) {
        ResponseList<Event> responseList = new ResponseList<>();
        Condition condition = trueCondition();

        List<Event> list =  eventRepository.fetch(condition, page, pageSize);

        responseList.setList(list);
        responseList.setTotal(eventRepository.count(condition));
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    @Transactional
    public Event create(EventDTO eventDTO) {

        if (Objects.isNull(eventDTO.getLocation()) && Objects.nonNull(eventDTO.getLatitude()) && Objects.nonNull(eventDTO.getLongitude()))
            eventDTO.setLocation(mapService.getAddress(eventDTO.getLatitude(), eventDTO.getLongitude()));

        if (Objects.nonNull(eventDTO.getLocation()) && Objects.isNull(eventDTO.getLatitude()) && Objects.isNull(eventDTO.getLongitude())) {
            var coordinates = mapService.getCoordinates(eventDTO.getLocation());
            eventDTO.setLatitude(coordinates.getLatitude());
            eventDTO.setLongitude(coordinates.getLongitude());
        }

        Event event = eventMapper.dtoToEvent(eventDTO);
        eventDao.insert(event);

        if (Objects.nonNull(eventDTO.getFiles())) {
            eventDTO.getFiles().forEach(file -> {
                file.setEventId(event.getId());
            });
        }
        eventFileDao.insert(eventDTO.getFiles().stream().map(eventFileMapper::toEntity).toList());
        return event;
    }

    public EventDTO get(Long id) {
        EventDTO eventDTO = eventMapper.toDto(eventDao.fetchOptionalById(id)
                .orElseThrow(() -> new EventNotFoundException(id)));
        eventDTO.setFiles(eventFileDao.fetchByEventId(id).stream().map(eventFileMapper::toDto).collect(Collectors.toSet()));
        return eventDTO;
    }

    @Transactional
    public Event update(Long id, EventDTO eventDTO) {
        Event event = eventDao.findOptionalById(id).orElseThrow(() -> new UserNotFoundException(id));

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
    public boolean delete(Long id) {
        eventDao.deleteById(id);
        return true;
    }

    public List<Long> addTagsToEvent(Long eventId, List<TagDTO> tagNamesDTO) {

        System.out.printf("Formatted value: %d", eventId);
        List <Tag> tagNames=tagNamesDTO.stream().map(tagMapper::toEntity).toList();
        Event event = eventDao.findOptionalById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        //1. Добавление новых тегов, получение id всех тегов из запроса
        List<Tag> tagWithId = tagService.checkAllTags(tagNames);

        //2. Получение списка использованных тегов для мероприятия
        Set<Long> usedTagIds =tagService.getUsedTagIdsForEvent(eventId);

        //3. Получение id неиспользованных тегов
        List<Long> newTagIds = tagWithId.stream()
                .map(Tag::getId)
                .filter(tagId -> !usedTagIds.contains((tagId)))
                .toList();

        //4. Добавление связи для новых тегов и мероприятия
        if (!newTagIds.isEmpty()) {
            tagService.assignTagsToEvent(eventId, newTagIds);
        }

        return newTagIds;
    }


}
