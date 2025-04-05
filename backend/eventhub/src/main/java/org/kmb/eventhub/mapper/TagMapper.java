package org.kmb.eventhub.mapper;

import org.kmb.eventhub.dto.TagDTO;
import org.kmb.eventhub.tables.pojos.Tag;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface TagMapper {
    TagDTO toDto(Tag tag);

    Tag toEntity(TagDTO tagDTO);
}
