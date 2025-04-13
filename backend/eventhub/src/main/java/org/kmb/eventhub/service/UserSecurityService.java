package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.enums.RoleEnum;
import org.kmb.eventhub.exception.UserNotFoundException;
import org.kmb.eventhub.repository.UserRepository;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class UserSecurityService {
    private UserRepository userRepository;

    public boolean isUserOwnData(Long userId) {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User authenticatedUser = userRepository.fetchByUsername(authenticatedUsername);

        if (Objects.isNull(authenticatedUser))
            throw new UserNotFoundException(userId);

        return authenticatedUser.getId().equals(userId) &&
                RoleEnum.MEMBER.name().equals(authenticatedUser.getRole().getLiteral());
    }
}
