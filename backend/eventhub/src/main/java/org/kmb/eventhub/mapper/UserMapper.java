package org.kmb.eventhub.mapper;

import org.kmb.eventhub.dto.UserDTO;
import org.kmb.eventhub.tables.pojos.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDto(User user);

    User toEntity(UserDTO userDTO);
}
