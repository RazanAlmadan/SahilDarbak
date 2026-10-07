package com.example.sahldarbak.ExternalApi;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.SmartItinerary.LocationDTO;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TransportationService {

    @Value("${geoapify.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.geoapify.com")
            .build();


    public JsonObject getRoute(
            LocationDTO from,
            LocationDTO to,
            String mode
    ) {

        try {

            String waypoints =
                    from.getLatitude() + ","
                            + from.getLongitude()
                            + "|"
                            + to.getLatitude() + ","
                            + to.getLongitude();


            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/routing")
                            .queryParam("waypoints", waypoints)
                            .queryParam("mode", mode)
                            .queryParam("format", "json")
                            .queryParam(
                                    "details",
                                    "instruction_details"
                            )
                            .queryParam("apiKey", apiKey)
                            .build())
                    .retrieve()
                    .body(String.class);


            if (response == null) {
                throw new ApiException(
                        "empty transportation API response"
                );
            }


            JsonObject json =
                    JsonParser
                            .parseString(response)
                            .getAsJsonObject();


            if (!json.has("results")
                    || json.getAsJsonArray("results").isEmpty()) {

                throw new ApiException(
                        "no route found for transportation mode: "
                                + mode
                );
            }


            return json
                    .getAsJsonArray("results")
                    .get(0)
                    .getAsJsonObject();


        } catch (Exception e) {

            if (e instanceof ApiException) {
                throw (ApiException) e;
            }

            throw new ApiException(
                    "failed to get transportation route: "
                            + e.getMessage()
            );
        }
    }


    public JsonObject getWalkingRoute(
            LocationDTO from,
            LocationDTO to
    ) {

        return getRoute(
                from,
                to,
                "walk"
        );
    }


    public JsonObject getDrivingRoute(
            LocationDTO from,
            LocationDTO to
    ) {

        return getRoute(
                from,
                to,
                "drive"
        );
    }


    public JsonObject getPublicTransportRoute(
            LocationDTO from,
            LocationDTO to
    ) {

        try {

            return getRoute(
                    from,
                    to,
                    "transit"
            );

        } catch (ApiException e) {

            // Public transportation is optional.
            // If Geoapify cannot provide transit data,
            // do not stop the entire transportation generation.

            JsonObject unavailable = new JsonObject();

            unavailable.addProperty(
                    "available",
                    false
            );

            unavailable.addProperty(
                    "message",
                    "Public transportation route data is unavailable."
            );

            return unavailable;
        }
    }
}