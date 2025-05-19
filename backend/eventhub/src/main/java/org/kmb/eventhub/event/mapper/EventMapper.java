package org.kmb.eventhub.event.mapper;

import org.kmb.eventhub.event.dto.EventDTO;
import org.kmb.eventhub.tables.pojos.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "files", ignore = true)
    @Mapping(target = "tags", ignore = true)
    EventDTO toDto(Event event);

    Event dtoToEvent(EventDTO eventDTO);

}
