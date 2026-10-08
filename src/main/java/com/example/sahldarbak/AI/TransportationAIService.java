package com.example.sahldarbak.AI;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.SmartItinerary.LocationDTO;
import com.example.sahldarbak.DTO.TransportationRouteDTO;
import com.example.sahldarbak.ExternalApi.GeoapifyService;
import com.example.sahldarbak.ExternalApi.TransportationService;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Model.TripPlace;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransportationAIService {

    private final TransportationService transportationService;
    private final GeoapifyService geoapifyService;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl(
                    "https://generativelanguage.googleapis.com"
            )
            .build();


    public TransportationRouteDTO generateTransportation(
            TripPlace fromPlace,
            TripPlace toPlace,
            String country,
            TravelRequest travelRequest
    ) {

        try {

            /*
             * STEP 1
             * Get coordinates for both places.
             */

            LocationDTO fromLocation =
                    geoapifyService.getCoordinates(
                            fromPlace.getName()
                                    + ", "
                                    + fromPlace.getCity(),
                            country
                    );


            LocationDTO toLocation =
                    geoapifyService.getCoordinates(
                            toPlace.getName()
                                    + ", "
                                    + toPlace.getCity(),
                            country
                    );


            /*
             * STEP 2
             * Get actual route information.
             */

            JsonObject walkingRoute =
                    transportationService
                            .getWalkingRoute(
                                    fromLocation,
                                    toLocation
                            );


            JsonObject drivingRoute =
                    transportationService
                            .getDrivingRoute(
                                    fromLocation,
                                    toLocation
                            );


            JsonObject publicTransportRoute =
                    transportationService
                            .getPublicTransportRoute(
                                    fromLocation,
                                    toLocation
                            );


            /*
             * STEP 3
             * Give all real route data to Gemini.
             */

            String prompt =
                    buildPrompt(
                            fromPlace,
                            toPlace,
                            travelRequest,
                            walkingRoute,
                            drivingRoute,
                            publicTransportRoute
                    );


            /*
             * STEP 4
             * Ask Gemini to choose the best option.
             */

            String response =
                    callGemini(prompt);


            /*
             * STEP 5
             * Convert Gemini JSON to DTO.
             */

            return objectMapper.readValue(
                    response,
                    TransportationRouteDTO.class
            );


        } catch (ApiException e) {

            throw e;

        } catch (Exception e) {

            throw new ApiException(
                    "failed to generate transportation: "
                            + e.getMessage()
            );
        }
    }


    private String buildPrompt(
            TripPlace fromPlace,
            TripPlace toPlace,
            TravelRequest travelRequest,
            JsonObject walkingRoute,
            JsonObject drivingRoute,
            JsonObject publicTransportRoute
    ) throws Exception {


        String preferences =
                buildTravelPreferences(
                        travelRequest
                );


        return """
                You are the transportation assistant for Sahl Darbak.

                Your job is to recommend the best way for a traveler
                to move from one planned destination to another.

                IMPORTANT:

                1. Use ONLY the route information provided below.

                2. NEVER invent:
                   - transportation lines
                   - metro lines
                   - train lines
                   - bus numbers
                   - station names
                   - travel times
                   - distances
                   - prices

                3. If the public transportation data does not contain
                   a specific line or service name, return null for "line".

                4. If public transportation is available, explain
                   what type of public transportation the traveler
                   should use when the route data supports it.

                5. Consider the user's preferences and restrictions.

                6. Consider the user's trip pace.

                7. Prefer practical recommendations rather than
                   simply choosing the fastest option.

                8. Walking should be avoided if the user has an
                   accessibility restriction or another restriction
                   that makes long walking unsuitable.

                Respond entirely in Arabic.
                Return the response in the required JSON format.
                Do not add any extra text outside the JSON.
                
                9. Return ONLY valid JSON.

                10. Do not use Markdown.

                Use EXACTLY this structure:

                {
                  "from": "Place A",
                  "to": "Place B",
                  "recommendedMethod": "PUBLIC_TRANSPORT",
                  "transportType": "Metro",
                  "line": null,
                  "instructions": "Use public transportation according to the available route information.",
                  "distanceKm": 4.5,
                  "durationMinutes": 22,
                  "reason": "🚇 Public transportation is a good fit because it avoids a long walk while remaining practical."
                }

                =========================
                FROM
                =========================

                Name:
                """ + fromPlace.getName() + """

                City:
                """ + fromPlace.getCity() + """

                =========================
                TO
                =========================

                Name:
                """ + toPlace.getName() + """

                City:
                """ + toPlace.getCity() + """

                =========================
                USER PREFERENCES
                =========================

                """ + preferences + """

                =========================
                WALKING ROUTE
                =========================

                """ + walkingRoute + """

                =========================
                DRIVING ROUTE
                =========================

                """ + drivingRoute + """

                =========================
                PUBLIC TRANSPORT ROUTE
                =========================
                
                """ + publicTransportRoute + """
                

                =========================

                Choose the most suitable transportation method
                for this traveler.
                """;
    }


    private String buildTravelPreferences(
            TravelRequest travelRequest
    ) {

        StringBuilder text =
                new StringBuilder();


        if (travelRequest.getGeneralPreference() != null) {

            text.append(
                    "Weather preference: "
            ).append(
                    travelRequest
                            .getGeneralPreference()
                            .getWeather()
            ).append("\n");

            text.append(
                    "Environment: "
            ).append(
                    travelRequest
                            .getGeneralPreference()
                            .getEnvironment()
            ).append("\n");

            text.append(
                    "Crowd preference: "
            ).append(
                    travelRequest
                            .getGeneralPreference()
                            .getCrowdPreference()
            ).append("\n");

            text.append(
                    "Trip pace: "
            ).append(
                    travelRequest
                            .getGeneralPreference()
                            .getTripPace()
            ).append("\n");
        }


        text.append("\nACTIVITIES:\n");

        if (travelRequest.getActivityPreferences() != null) {

            travelRequest
                    .getActivityPreferences()
                    .forEach(activity ->
                            text.append("- ")
                                    .append(
                                            activity
                                                    .getActivityType()
                                    )
                                    .append(" | priority: ")
                                    .append(
                                            activity
                                                    .getPriority()
                                    )
                                    .append("\n")
                    );
        }


        text.append("\nRESTRICTIONS:\n");

        if (travelRequest.getTravelRestrictions() != null) {

            travelRequest
                    .getTravelRestrictions()
                    .forEach(restriction ->
                            text.append("- ")
                                    .append(
                                            restriction
                                                    .getRestrictionType()
                                    )
                                    .append(": ")
                                    .append(
                                            restriction
                                                    .getDescription()
                                    )
                                    .append(" | required: ")
                                    .append(
                                            restriction
                                                    .getIsRequired()
                                    )
                                    .append("\n")
                    );
        }


        return text.toString();
    }


    private String callGemini(
            String prompt
    ) throws Exception {


        Map<String, Object> generationConfig =
                Map.of(
                        "responseMimeType",
                        "application/json"
                );


        Map<String, Object> requestBody =
                Map.of(
                        "contents",
                        List.of(
                                Map.of(
                                        "parts",
                                        List.of(
                                                Map.of(
                                                        "text",
                                                        prompt
                                                )
                                        )
                                )
                        ),
                        "generationConfig",
                        generationConfig
                );


        Map response =
                restClient.post()
                        .uri(
                                "/v1beta/models/"
                                        + "gemini-3.5-flash-lite:"
                                        + "generateContent"
                        )
                        .header(
                                "x-goog-api-key",
                                apiKey
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .body(requestBody)
                        .retrieve()
                        .body(Map.class);


        if (response == null) {
            throw new ApiException(
                    "Gemini returned an empty response"
            );
        }


        List<Map<String, Object>> candidates =
                (List<Map<String, Object>>)
                        response.get("candidates");


        if (candidates == null
                || candidates.isEmpty()) {

            throw new ApiException(
                    "Gemini did not generate transportation"
            );
        }


        Map<String, Object> content =
                (Map<String, Object>)
                        candidates
                                .get(0)
                                .get("content");


        List<Map<String, Object>> parts =
                (List<Map<String, Object>>)
                        content.get("parts");


        if (parts == null
                || parts.isEmpty()) {

            throw new ApiException(
                    "invalid Gemini response"
            );
        }


        return ((String)
                parts.get(0).get("text"))
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }
}
