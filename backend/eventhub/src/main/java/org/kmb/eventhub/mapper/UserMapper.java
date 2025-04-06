package org.kmb.eventhub.mapper;

import org.kmb.eventhub.dto.MemberDTO;
import org.kmb.eventhub.dto.ModeratorDTO;
import org.kmb.eventhub.dto.OrganizerDTO;
import org.kmb.eventhub.dto.UserDTO;
import org.kmb.eventhub.tables.pojos.Member;
import org.kmb.eventhub.tables.pojos.Moderator;
import org.kmb.eventhub.tables.pojos.Organizer;
import org.kmb.eventhub.tables.pojos.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDto(User user);

    User toEntity(UserDTO userDTO);

    Organizer toOrganizer(User user);

    Member toMember(User user);

    Moderator toModerator(User user);

    Organizer dtoToOrganizer(OrganizerDTO organizerDTO);

    Member dtoToMember(MemberDTO memberDTO);

    Moderator dtoToModerator(ModeratorDTO moderatorDTO);

}
