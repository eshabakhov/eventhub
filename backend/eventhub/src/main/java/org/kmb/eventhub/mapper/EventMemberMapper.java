package org.kmb.eventhub.mapper;

import org.kmb.eventhub.dto.EventMemberDTO;
import org.kmb.eventhub.tables.pojos.EventMembers;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMemberMapper {

    EventMemberDTO toDto(EventMembers eventMember);

    EventMembers dtoToEvent(EventMemberDTO eventMemberDTO);

}
