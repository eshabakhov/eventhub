package org.kmb.eventhub.user.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.user.enums.RoleEnum;
import org.kmb.eventhub.user.exception.UserNotFoundException;
import org.kmb.eventhub.user.repository.ModeratorRepository;
import org.kmb.eventhub.user.repository.UserRepository;
import org.kmb.eventhub.tables.pojos.Moderator;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class AdminSecurityService {

    private ModeratorRepository moderatorRepository;
    private UserRepository userRepository;

    public boolean isAdmin() {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User authenticatedUser = userRepository.fetchByUsername(authenticatedUsername);

        long id = authenticatedUser.getId();
        Moderator moderator = moderatorRepository.findById(id);

        if (Objects.isNull(moderator))
            throw new UserNotFoundException(id);

        return moderator.getIsAdmin() &&
                RoleEnum.MODERATOR.name().equals(authenticatedUser.getRole().getLiteral());

    }
}
