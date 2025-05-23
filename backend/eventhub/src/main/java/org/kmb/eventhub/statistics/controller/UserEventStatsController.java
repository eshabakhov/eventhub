package org.kmb.eventhub.statistics.controller;

import org.kmb.eventhub.statistics.service.UserEventStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/stats")
public class UserEventStatsController {
    private final UserEventStatsService statsService;

    public UserEventStatsController(UserEventStatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/event/{eventId}/views")
    public int getTotalViewsForEvent(@PathVariable Long eventId) {
        return statsService.getTotalViewsForEvent(eventId);
    }

    @GetMapping("/user/{userId}/views")
    public int getTotalViewsByUser(@PathVariable Long userId) {
        return statsService.getTotalViewsByUser(userId);
    }

    @GetMapping("/event/{eventId}/user/{userId}/views")
    public int getViewsByUserForEvent(@PathVariable Long eventId, @PathVariable Long userId) {
        return statsService.getViewsByUserForEvent(userId, eventId);
    }

    @GetMapping("/organizers/{organizerId}/views")
    public Long getAllViews(@PathVariable Long organizerId) {
        return statsService.getAllViews(organizerId);
    }

    @GetMapping("/organizers/{organizerId}/favorites")
    public Long getOrganizerFavorites(@PathVariable Long organizerId) {
        return statsService.getOrganizerFavorites(organizerId);
    }
    @GetMapping("/organizers/{organizerId}/members")
    public Long getMembersCount(@PathVariable Long organizerId) {
        return statsService.getOrganizerMembers(organizerId);
    }

    @GetMapping("/organizers/{organizerId}/events")
    public Long getEventsCount(@PathVariable Long organizerId) {
        return statsService.getOrganizerEvents(organizerId);
    }
}
