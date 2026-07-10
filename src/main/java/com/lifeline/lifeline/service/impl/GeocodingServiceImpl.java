package com.lifeline.lifeline.service.impl;

import com.lifeline.lifeline.exception.InvalidTokenException;
import com.lifeline.lifeline.service.GeocodingService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GeocodingServiceImpl implements GeocodingService {

    private final WebClient webClient;

    public GeocodingServiceImpl() {
        this.webClient = WebClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader("User-Agent", "LifelineAI/1.0 (student-project)")
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public double[] geocodeAddress(String address) {
        List<Map<String, Object>> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", address)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        if (response == null || response.isEmpty()) {
            throw new InvalidTokenException("Could not locate this address. Please check it and try again.");
        }

        Map<String, Object> result = response.get(0);
        double lat = Double.parseDouble((String) result.get("lat"));
        double lon = Double.parseDouble((String) result.get("lon"));

        return new double[]{lon, lat};
    }
}