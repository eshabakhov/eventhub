package org.kmb.eventhub.user.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.enums.RoleType;
import org.kmb.eventhub.tables.daos.ModeratorDao;
import org.kmb.eventhub.tables.pojos.Moderator;
import org.kmb.eventhub.user.enums.RoleEnum;
import org.kmb.eventhub.user.exception.UserNotFoundException;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserSecurityService {

    private final ModeratorDao moderatorDao;

    public boolean isUserOwnData(Long userId, User authenticatedUser) {

        if (Objects.isNull(authenticatedUser))
            throw new UserNotFoundException(userId);

        if (RoleType.MODERATOR.equals(authenticatedUser.getRole())) {
            Optional<Moderator> moderator = moderatorDao.fetchOptionalById(authenticatedUser.getId());
            if (moderator.isPresent()) {
                return true;
            }
        }

        return authenticatedUser.getId().equals(userId);
    }
}
