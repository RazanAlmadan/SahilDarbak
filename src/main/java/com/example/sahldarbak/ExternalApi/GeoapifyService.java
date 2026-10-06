package com.example.sahldarbak.ExternalApi;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.SmartItinerary.LocationDTO;
import com.example.sahldarbak.DTO.SmartItinerary.PlaceOptionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeoapifyService {

    @Value("${geoapify.api-key}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.geoapify.com")
            .build();


    // GET COORDINATES FOR A CITY AND COUNTRY USING GEOAPIFY
    public LocationDTO getCoordinates(String city, String country) {

        Map response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/geocode/search")
                        .queryParam("text", city + ", " + country)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .queryParam("apiKey", apiKey)
                        .build())
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new ApiException("failed to get location from Geoapify");
        }

        List<Map<String, Object>> results =
                (List<Map<String, Object>>) response.get("results");

        if (results == null || results.isEmpty()) {
            throw new ApiException("location not found");
        }

        Map<String, Object> result = results.get(0);

        return new LocationDTO(
                city,
                country,
                ((Number) result.get("lat")).doubleValue(),
                ((Number) result.get("lon")).doubleValue()
        );
    }


    // GET PLACES BY CATEGORY
    private List<PlaceOptionDTO> getPlaces(
            Double latitude,
            Double longitude,
            String category,
            String type,
            Integer limit) {

        Map response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/places")
                        .queryParam("categories", category)
                        .queryParam(
                                "filter",
                                "circle:" + longitude + "," + latitude + ",8000"
                        )
                        .queryParam(
                                "bias",
                                "proximity:" + longitude + "," + latitude
                        )
                        .queryParam("limit", limit)
                        .queryParam("apiKey", apiKey)
                        .build())
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new ApiException("failed to get places from Geoapify");
        }

        List<Map<String, Object>> features =
                (List<Map<String, Object>>) response.get("features");

        List<PlaceOptionDTO> places = new ArrayList<>();

        if (features == null || features.isEmpty()) {
            return places;
        }

        for (Map<String, Object> feature : features) {

            Map<String, Object> properties =
                    (Map<String, Object>) feature.get("properties");

            if (properties == null || properties.get("name") == null) {
                continue;
            }

            places.add(new PlaceOptionDTO(
                    (String) properties.get("place_id"),
                    (String) properties.get("name"),
                    type,
                    (String) properties.get("formatted"),
                    properties.get("lat") == null
                            ? null
                            : ((Number) properties.get("lat")).doubleValue(),
                    properties.get("lon") == null
                            ? null
                            : ((Number) properties.get("lon")).doubleValue(),
                    properties.get("distance") == null
                            ? null
                            : ((Number) properties.get("distance")).doubleValue(),
                    (String) properties.get("website"),

                    // ratings
                    null,

                    // estimated price
                    null,

                    // halal info
                    null
            ));
        }

        return places;
    }


    // GET NEARBY HOTELS
    public List<PlaceOptionDTO> getHotels(
            Double latitude,
            Double longitude) {

        return getPlaces(
                latitude,
                longitude,
                "accommodation.hotel",
                "hotel",
                10
        );
    }


    // GET BETTER TOURIST ACTIVITIES
    public List<PlaceOptionDTO> getActivities(
            Double latitude,
            Double longitude) {

        List<PlaceOptionDTO> allActivities = new ArrayList<>();

        // MUSEUMS
        allActivities.addAll(
                getPlaces(
                        latitude,
                        longitude,
                        "entertainment.museum",
                        "activity",
                        10
                )
        );

        // CULTURAL PLACES
        allActivities.addAll(
                getPlaces(
                        latitude,
                        longitude,
                        "entertainment.culture",
                        "activity",
                        10
                )
        );

        // HISTORICAL / IMPORTANT SIGHTS
        allActivities.addAll(
                getPlaces(
                        latitude,
                        longitude,
                        "tourism.sights",
                        "activity",
                        10
                )
        );

        // SHOPPING
        allActivities.addAll(
                getPlaces(
                        latitude,
                        longitude,
                        "commercial.shopping_mall",
                        "activity",
                        5
                )
        );

        return removeDuplicatePlaces(allActivities);
    }


    // REMOVE DUPLICATE OR VERY SIMILAR PLACE NAMES
    private List<PlaceOptionDTO> removeDuplicatePlaces(
            List<PlaceOptionDTO> places) {

        Map<String, PlaceOptionDTO> uniquePlaces =
                new LinkedHashMap<>();

        for (PlaceOptionDTO place : places) {

            if (place.getName() == null) {
                continue;
            }

            String normalizedName =
                    place.getName()
                            .trim()
                            .toLowerCase();

            uniquePlaces.putIfAbsent(
                    normalizedName,
                    place
            );
        }

        return new ArrayList<>(uniquePlaces.values());
    }


    // GET NEARBY RESTAURANTS
    public List<PlaceOptionDTO> getRestaurants(
            Double latitude,
            Double longitude) {

        return getPlaces(
                latitude,
                longitude,
                "catering.restaurant",
                "restaurant",
                15
        );
    }
}