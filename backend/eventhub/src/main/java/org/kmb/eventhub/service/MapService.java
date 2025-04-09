package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.CoordinatesDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class MapService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            .defaultHeader("User-Agent", "YourAppName/1.0")
            .build();


    public String getAddress(BigDecimal latitude, BigDecimal longitude) {
        String uri = "/reverse?format=json&lat=" + latitude + "&lon=" + longitude;

        Map<String, Object> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return (String) response.getOrDefault("display_name", "Адрес не найден");
    }
    public CoordinatesDTO getCoordinates(String address) {
        String uri = "/search?q=" + address + "&format=json";

        var response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        if (response != null && !response.isEmpty()) {
            LinkedHashMap<String, Object> result;
            result = (LinkedHashMap<String, Object>) response.get(0);
            BigDecimal lat = new BigDecimal(String.valueOf(result.get("lat")));
            BigDecimal lon = new BigDecimal(String.valueOf(result.get("lon")));
            return new CoordinatesDTO(lat, lon);
        }
        return null;
    }
}
