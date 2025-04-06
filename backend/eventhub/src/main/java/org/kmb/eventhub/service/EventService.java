package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.repository.EventRepository;
import org.kmb.eventhub.tables.daos.EventDao;
import org.kmb.eventhub.tables.pojos.Event;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.jooq.impl.DSL.trueCondition;
@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private final EventDao eventDao;

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
        eventDao.insert(event);
        return event;
    }

    public Event get(Long id) {
        return eventDao.findById(id);
    }

    public Event update(Event event) {
        eventDao.update(event);
        return event;
    }

    public boolean delete(Long id) {
        eventDao.deleteById(id);
        return true;
    }
}
