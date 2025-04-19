package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.enums.RoleEnum;
import org.kmb.eventhub.exception.EventNotFoundException;
import org.kmb.eventhub.repository.EventRepository;
import org.kmb.eventhub.repository.UserRepository;
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class EventSecurityService {
    private UserRepository userRepository;
    private EventRepository eventRepository;

    public boolean isUserOwnEvent(Long eventId) {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User authenticatedUser = userRepository.fetchByUsername(authenticatedUsername);

        Event event = eventRepository.fetchById(eventId);

        if (Objects.isNull(event))
            throw new EventNotFoundException(eventId);

        return authenticatedUser.getId().equals(event.getOrganizerId()) &&
                RoleEnum.ORGANIZER.name().equals(authenticatedUser.getRole().getLiteral());
    }
}
