package org.kmb.eventhub.recommendation.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.auth.service.UserDetailsService;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.event.dto.EventDTO;
import org.kmb.eventhub.event.enums.EventFormat;
import org.kmb.eventhub.event.mapper.EventMapper;
import org.kmb.eventhub.event.repository.EventRepository;
import org.kmb.eventhub.recommendation.repository.RecommendationRepository;
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tag.mapper.TagMapper;
import org.kmb.eventhub.tag.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.trueCondition;

@Service
@AllArgsConstructor
public class RecommendationService {

    private final UserDetailsService UserDetailsService;

    private final RecommendationRepository recommendationRepository;

    private final EventRepository eventRepository;

    private final TagRepository tagRepository;

    private final EventMapper eventMapper;

    private final TagMapper tagMapper;

    private Condition getCommonListCondition(String search, List<String> tags) {
        Condition condition = trueCondition();

        if (Objects.nonNull(search) && !search.trim().isEmpty()) {
            condition = condition.and(org.kmb.eventhub.tables.Event.EVENT.TITLE.containsIgnoreCase(search));
            condition = condition.or(org.kmb.eventhub.tables.Event.EVENT.SHORT_DESCRIPTION.containsIgnoreCase(search));
            condition = condition.or(org.kmb.eventhub.tables.Event.EVENT.LOCATION.containsIgnoreCase(search));


            Map<String, String> formatRuMap = Map.of(
                    "ONLINE", "Онлайн",
                    "OFFLINE", "Офлайн"
            );

            List<EventFormat> matchingFormats = formatRuMap.entrySet().stream()
                    .filter(entry -> entry.getValue().toLowerCase().contains(search.toLowerCase()))
                    .map(entry -> EventFormat.valueOf(entry.getKey()))
                    .toList();

            if (!matchingFormats.isEmpty()) {
                condition = condition.or(org.kmb.eventhub.tables.Event.EVENT.FORMAT.in(matchingFormats));
            }

        }
        if (Objects.nonNull(tags) && !tags.isEmpty()) {
            condition = condition.and(org.kmb.eventhub.tables.Event.EVENT.ID.in(recommendationRepository.fetchEventIdsBySelectedTags(tags)));
        }

        return condition;
    }


    public ResponseList<EventDTO> getRecommendedEvents(Integer page, Integer pageSize, String search, List<String> tags, double lat, double lon) {

        Long userId = UserDetailsService.getAuthenticatedUser().getId();

        Condition condition = getCommonListCondition(search, tags);

        List<Event> recommendations = recommendationRepository.getRecommendedEventsCond(userId,lon,lat,page,pageSize,condition);

        System.out.println(recommendations);


        ResponseList<EventDTO> responseList = new ResponseList<>();
        List<EventDTO> eventDTOList = new ArrayList<>();
        recommendations.forEach(event -> {
            EventDTO eventDTO = eventMapper.toDto(event);
            eventDTO.setTags(tagRepository.fetch(event.getId()).stream().map(tagMapper::toDto).collect(Collectors.toSet()));
            eventDTOList.add(eventDTO);
        });

        responseList.setList(eventDTOList);
        responseList.setTotal(eventRepository.count(condition));
        responseList.setPageSize(pageSize);
        return responseList;
    }
}
