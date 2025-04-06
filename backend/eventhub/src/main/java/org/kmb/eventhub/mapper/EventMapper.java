package org.kmb.eventhub.mapper;

import org.kmb.eventhub.dto.EventDTO;
import org.kmb.eventhub.tables.pojos.Event;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventDTO toDto(Event event);

    Event toEntity(EventDTO eventDTO);

}
