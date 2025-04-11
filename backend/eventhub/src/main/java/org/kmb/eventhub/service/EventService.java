package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.dto.EventDTO;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.enums.FormatType;
import org.kmb.eventhub.exception.AlreadyExistsException;
import org.kmb.eventhub.exception.EventNotFoundException;
import org.kmb.eventhub.exception.UserNotFoundException;
import org.kmb.eventhub.mapper.EventMapper;
import org.kmb.eventhub.mapper.EventFileMapper;
import org.kmb.eventhub.repository.EventRepository;
import org.kmb.eventhub.tables.daos.EventDao;
import org.kmb.eventhub.tables.daos.EventFileDao;
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tables.pojos.EventFile;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.trueCondition;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private final EventDao eventDao;

    private final EventMapper eventMapper;

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
}
