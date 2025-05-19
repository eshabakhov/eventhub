package org.kmb.eventhub.event.mapper;

import org.kmb.eventhub.event.dto.EventFileDTO;
import org.kmb.eventhub.tables.pojos.EventFile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventFileMapper {
    EventFileDTO toDto(EventFile file);

    EventFile toEntity(EventFileDTO fileDTO);
}
