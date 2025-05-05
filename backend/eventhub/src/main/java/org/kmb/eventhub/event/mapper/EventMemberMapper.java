package org.kmb.eventhub.event.mapper;

import org.kmb.eventhub.event.dto.EventMemberDTO;
import org.kmb.eventhub.tables.pojos.EventMembers;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMemberMapper {

    @Mapping(source = "memberId", target = "userId")
    EventMemberDTO toDto(EventMembers eventMember);

    EventMembers dtoToEvent(EventMemberDTO eventMemberDTO);

}
