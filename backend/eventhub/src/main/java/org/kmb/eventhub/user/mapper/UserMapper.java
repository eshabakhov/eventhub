package org.kmb.eventhub.user.mapper;

import org.kmb.eventhub.user.dto.MemberDTO;
import org.kmb.eventhub.user.dto.ModeratorDTO;
import org.kmb.eventhub.user.dto.OrganizerDTO;
import org.kmb.eventhub.user.dto.UserDTO;
import org.kmb.eventhub.tables.pojos.Member;
import org.kmb.eventhub.tables.pojos.Moderator;
import org.kmb.eventhub.tables.pojos.Organizer;
import org.kmb.eventhub.tables.pojos.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDto(User user);

    User toEntity(UserDTO userDTO);

    @Mapping(target = "name", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "industry", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "isAccredited", ignore = true)
    Organizer toOrganizer(User user);

    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "patronymic", ignore = true)
    @Mapping(target = "birthDate", ignore = true)
    @Mapping(target = "birthCity", ignore = true)
    @Mapping(target = "privacy", ignore = true)
    Member toMember(User user);

    @Mapping(target = "isAdmin", ignore = true)
    Moderator toModerator(User user);

    Organizer dtoToOrganizer(OrganizerDTO organizerDTO);

    Member dtoToMember(MemberDTO memberDTO);

    MemberDTO toMemberDto(Member member);

    Moderator dtoToModerator(ModeratorDTO moderatorDTO);

}
