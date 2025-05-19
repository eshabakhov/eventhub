package org.kmb.eventhub.recommendation.controller;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.recommendation.service.RecommendationService;
import org.kmb.eventhub.event.dto.EventDTO;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value ="/v1/recommend")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseList<EventDTO> getRecommendations(
            @RequestParam(value = "lat") double latitude,
            @RequestParam(value = "lon") double longitude,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "tags", required = false) List<String> tags
            ) {
        return recommendationService.getRecommendedEvents(page, pageSize, search, tags, latitude, longitude);
    }
}
