package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.dto.EventDTO;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.exception.UserNotFoundException;
import org.kmb.eventhub.mapper.EventMapper;
import org.kmb.eventhub.mapper.UserMapper;
import org.kmb.eventhub.repository.EventRepository;
import org.kmb.eventhub.tables.daos.EventDao;
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tables.pojos.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static org.jooq.impl.DSL.trueCondition;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private final EventDao eventDao;

    private final EventMapper eventMapper;

    private final MapService mapService;

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

    public Event create(Event event) {
        if (Objects.isNull(event.getLocation()) && Objects.nonNull(event.getLatitude()) && Objects.nonNull(event.getLongitude()))
            event.setLocation(mapService.getAddress(event.getLatitude(), event.getLongitude()));

        if (Objects.nonNull(event.getLocation()) && Objects.isNull(event.getLatitude()) && Objects.isNull(event.getLongitude())) {
            var coordinates = mapService.getCoordinates(event.getLocation());
            event.setLatitude(coordinates.getLatitude());
            event.setLongitude(coordinates.getLongitude());
        }
        eventDao.insert(event);
        return event;
    }

    public Event get(Long id) {
        return eventDao.findById(id);
    }

    @Transactional
    public Event update(Long id, EventDTO eventDTO) {
        Event event = eventDao.findOptionalById(id).orElseThrow(() -> new UserNotFoundException(id));
        //Event event = eventMapper.dtoToEvent(eventDTO);
        event.setId(id);
        eventDao.update(event);
        return event;
    }

    public boolean delete(Long id) {
        eventDao.deleteById(id);
        return true;
    }
}
