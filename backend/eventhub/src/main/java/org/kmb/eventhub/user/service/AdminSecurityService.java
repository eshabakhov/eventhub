package org.kmb.eventhub.user.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.user.enums.RoleEnum;
import org.kmb.eventhub.user.exception.UserNotFoundException;
import org.kmb.eventhub.user.repository.ModeratorRepository;
import org.kmb.eventhub.tables.pojos.Moderator;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class AdminSecurityService {

    private ModeratorRepository moderatorRepository;

    public boolean isAdmin(User authenticatedUser) {
        long id = authenticatedUser.getId();
        Moderator moderator = moderatorRepository.findById(id);

        if (Objects.isNull(moderator))
            throw new UserNotFoundException(id);

        return moderator.getIsAdmin() &&
                RoleEnum.MODERATOR.name().equals(authenticatedUser.getRole().getLiteral());

    }
}
