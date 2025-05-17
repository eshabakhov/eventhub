package org.kmb.eventhub.recommendation.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.auth.service.UserDetailsService;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.event.dto.EventDTO;
import org.kmb.eventhub.event.mapper.EventMapper;
import org.kmb.eventhub.recommendation.repository.RecommendationRepository;
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tables.pojos.UserEventInteractions;
import org.kmb.eventhub.tag.mapper.TagMapper;
import org.kmb.eventhub.tag.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class RecommendationService {

    private final UserDetailsService UserDetailsService;

    private final RecommendationRepository recommendationRepository;

    private final TagRepository tagRepository;

    private final EventMapper eventMapper;

    private final TagMapper tagMapper;


    public ResponseList<EventDTO> getRecommendedEvents(double lat, double lon, Integer limitRec) {

        Long userId = UserDetailsService.getAuthenticatedUser().getId();

        List<Event> userEventsWithTags = recommendationRepository.findEventsByLocationAndUserInterests(userId,lat,	lon);

        List<UserEventInteractions> interactions = recommendationRepository.findInteractionsByUserId(userId);

        Set<Long> userTagIds = tagRepository.getUsedTagIdsForUser(userId);

        record ScoredEvent(Event event, double score) {}

        List<Event> recommendations = userEventsWithTags.stream()
                .map(event -> new ScoredEvent(event, calculateScore(event, userId, userTagIds, interactions, lat, lon)))
        .sorted(Comparator.comparingDouble(ScoredEvent::score).reversed())
                .limit(limitRec)
                .map(ScoredEvent::event)
                .toList();


        ResponseList<EventDTO> responseList = new ResponseList<>();
        List<EventDTO> eventDTOList = new ArrayList<>();
        recommendations.forEach(event -> {
            EventDTO eventDTO = eventMapper.toDto(event);
            eventDTO.setTags(tagRepository.fetch(event.getId()).stream().map(tagMapper::toDto).collect(Collectors.toSet()));
            eventDTOList.add(eventDTO);
        });

        responseList.setList(eventDTOList);
        responseList.setTotal((long) eventDTOList.size());
        responseList.setPageSize(limitRec);
        return responseList;
    }


    private double calculateScore(Event event, Long userId, Set<Long> userTagIds , List<UserEventInteractions> interactions, double userLat, double userLon) {
        Set<Long> eventTagIds = tagRepository.getUsedTagIdsForEvent(event.getId());

        long totalTags = Stream.concat(userTagIds.stream(), eventTagIds.stream())
                .distinct()
                .count();

        long commonTags = eventTagIds.stream()
                .filter(userTagIds::contains)
                .count();

        double matchRatio = totalTags > 0 ? (2.0 * commonTags) / totalTags : 0.0;


        double interactionScore = interactions.stream()
                .filter(i -> i.getEventId().equals(event.getId()))
                .mapToDouble(i -> switch (i.getInteractionType()) {
                    case "VIEW" -> 0.1;
                    case "FAVORITE" -> 1.0;
                    default -> 0.0;
                }).sum();

        double distance = calculateDistance( userLat, userLon, event.getLatitude(), event.getLongitude());

        double proximityScore = 1.0 / (1.0 + distance);

        return 0.3 * matchRatio + 0.2 * interactionScore + 0.2 * proximityScore;
    }

    public static double calculateDistance(double lat1, double lon1, BigDecimal lat2, BigDecimal lon2) {
        // Переводим градусы в радианы
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lon2Rad = Math.toRadians(lon2.doubleValue());

        // Разница координат
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Формула Haversine
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Расстояние
        return 6371 * c;
    }
}
