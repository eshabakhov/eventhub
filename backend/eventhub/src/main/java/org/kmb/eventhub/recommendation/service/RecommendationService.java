package org.kmb.eventhub.recommendation.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.auth.service.UserDetailsService;
import org.kmb.eventhub.event.dto.EventDTO;
import org.kmb.eventhub.event.service.EventService;
import org.kmb.eventhub.recommendation.dto.UserHistoryDTO;
import org.kmb.eventhub.recommendation.mapper.UserHistoryMapper;
import org.kmb.eventhub.recommendation.repository.RecommendationRepository;
import org.kmb.eventhub.tables.pojos.UserHistory;
import org.kmb.eventhub.tag.dto.TagDTO;
import org.kmb.eventhub.tag.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RecommendationService {

    private final UserDetailsService UserDetailsService;

    private final RecommendationRepository recommendationRepository;

    private final EventService eventService;

    private final TagRepository tagRepository;

    private final UserHistoryMapper userHistoryMapper;

    public List<EventDTO> getRecommendedEvents(Integer limitRec) {
        Long userId =  UserDetailsService.getAuthenticatedUser().getId();

        // 1. Получаем id избранных тегов
        Set<Long> userTagIds = tagRepository.getUsedTagIdsForUser(userId);


        // 2. Получаем историю просмотров
        List<UserHistory> userHistoryList = recommendationRepository.findByUser(userId);
        List<UserHistoryDTO> userHistoryDTOList = new ArrayList<>();
        userHistoryList.forEach(history -> {
            UserHistoryDTO userHistoryDTO = userHistoryMapper.toDto(history);
            userHistoryDTOList.add(userHistoryDTO);
        });

        List<EventDTO> allEvents = eventService.getAll();

        // 4. Формируем рекомендации
        return  allEvents.stream()
                .sorted((e1, e2) -> {
                    double score1 = calculateEventScore(e1, userTagIds, userHistoryDTOList);
                    double score2 = calculateEventScore(e2, userTagIds, userHistoryDTOList);
                    return Double.compare(score2, score1); // по убыванию
                })
                .limit(limitRec)
                .collect(Collectors.toList());
    }

    private double calculateEventScore(EventDTO event, Set<Long> userTagIds, List<UserHistoryDTO> userHistoryDTOList) {
        double score = 0;

        if (event.getTags() == null) {
            return score;
        }

        Set<Long> eventTagIds = event.getTags().stream()
                .map(TagDTO::getId)
                .collect(Collectors.toSet());

        for (Long tagId : userTagIds) {
            if (eventTagIds.contains(tagId)) {
                score += 10;
            }
        }

        for (UserHistoryDTO h : userHistoryDTOList) {
            if (h.getEventId().equals(event.getId())) {
                switch (h.getActionType()) {
                    case "VIEW":
                        score += 5;
                        break;
                    case "FAVORITE":
                        score += 10;
                        break;
                    case "RATE":
                        score += 20;
                        break;
                    case "SHARE":
                        score += 15;
                        break;
                    default:
                        break;
                }
            }
        }
        return score;
    }
}
