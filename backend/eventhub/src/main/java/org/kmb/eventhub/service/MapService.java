package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.CoordinatesDTO;
import org.kmb.eventhub.exception.AddressNotFound;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@AllArgsConstructor
public class MapService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            .defaultHeader("User-Agent", "EventHub/1.0")
            .build();


    public String getAddress(BigDecimal latitude, BigDecimal longitude, boolean isOnline) {
        String uri = "/reverse?format=json&lat=" + latitude + "&lon=" + longitude;

        Map<String, Object> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // Здесь превращаем адрес в строку Страна, Край, город, улица, номер дома
        Map<String, Object> addr = (Map<String, Object>) response.getOrDefault("address", Map.of());
        String country = (String) addr.getOrDefault("country", "");
        country = Objects.equals(country, "Россия") ? "" : country;
        String city = (String) addr.getOrDefault("city", "");
        String road = (String) addr.getOrDefault("road", "");
        String houseNumber = (String) addr.getOrDefault("house_number", "");
        String state = (String) addr.getOrDefault("state", "");

        StringBuilder sb = new StringBuilder();
        List<String> parts;
        // Если онлайн мероприятие, нужна только страна и город
        if (isOnline)
             parts = List.of(country, city);
        else
             parts= List.of(country, city, state, road, houseNumber);

        parts.stream()
                .filter(s -> !s.isEmpty())
                .reduce((s1, s2) -> s1 + ", " + s2)
                .ifPresent(sb::append);

        return sb.toString().trim();
    }
    public CoordinatesDTO getCoordinates(String address, boolean isOnline) {
        String uri;
        if (isOnline) {
            uri = "/search?city=" + address + "&format=json";
        }
        else uri = "/search?q=" + address + "&format=json";

        var response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        if (Objects.isNull(response) || response.isEmpty()) {
            throw new AddressNotFound(isOnline? String.format("City %s", address) : String.format("Address %s", address));
        }
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
