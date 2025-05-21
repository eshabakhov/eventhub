package org.kmb.eventhub.statistics.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.statistics.repository.UserEventInteractionRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserEventStatsService {

    private final UserEventInteractionRepository repository;

    public int getTotalViewsForEvent(Long eventId) {
        return repository.countViewsByEventId(eventId);
    }

    public int getTotalViewsByUser(Long userId) {
        return repository.countViewsByUserId(userId);
    }

    public int getViewsByUserForEvent(Long userId, Long eventId) {
        return repository.countViewsByUserAndEvent(userId, eventId);
    }

    public Long getAllViews(Long organizerId) {
        return repository.getAllViews(organizerId);
    }

    public Long getOrganizerFavorites(Long organizerId) {
        return repository.getOrganizerFavorites(organizerId);
    }

    public Long getOrganizerMembers(Long organizerId) {
        return repository.getAllMembers(organizerId);
    }

    public Long getOrganizerEvents(Long organizerId) {
        return repository.getAllEvents(organizerId);
    }
}

