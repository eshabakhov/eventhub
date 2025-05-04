package org.kmb.eventhub.event.mapper;

import org.kmb.eventhub.event.dto.EventDTO;
import org.kmb.eventhub.tables.pojos.Event;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventDTO toDto(Event event);

    Event dtoToEvent(EventDTO eventDTO);

}
