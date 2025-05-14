package org.kmb.eventhub.recommendation.controller;

import lombok.AllArgsConstructor;
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
    public List<EventDTO> getRecommendations(
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return recommendationService.getRecommendedEvents(pageSize);
    }
}
