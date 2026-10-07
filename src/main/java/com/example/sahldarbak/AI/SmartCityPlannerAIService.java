package com.example.sahldarbak.AI;



import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.CityPlan.CityPlanDTO;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Model.Trip;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class SmartCityPlannerAIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();


    // GENERATE INTELLIGENT MULTI-CITY PLAN
    public CityPlanDTO generateCityPlan(
            Trip trip,
            TravelRequest travelRequest,  String lang) {
        String outputLanguage =
                "en".equalsIgnoreCase(lang)
                        ? "English"
                        : "Arabic";

        long totalDays = ChronoUnit.DAYS.between(
                travelRequest.getStartDate(),
                travelRequest.getEndDate()) + 1;



        String prompt = """
        You are an intelligent and friendly travel city planner.

        Your job is to choose the best city or cities INSIDE the given country
        and divide the available trip days intelligently.

        The result will later be used by another system to build the detailed
        daily itinerary.

        Your recommendations should feel like they come from a smart,
        helpful travel companion rather than a formal report.
                ========================
                OUTPUT LANGUAGE
                ========================
                
                The selected UI language is: %s
                
                Write all user-facing descriptive text in %s.
                
                For Arabic:
                - Use clear, natural and friendly Arabic.
                - Keep the tone warm and concise.
                - Do not use overly formal language.
                
                IMPORTANT:
                Do NOT translate or modify:
                - place names
                - hotel names
                - restaurant names
                - attraction names
                - city names
                - URLs
                - source names
                - external IDs
                - enum/status values
                - JSON keys
                - factual evidence copied from external sources
                
                Do NOT change the required JSON structure.

        ========================
        TRIP INFORMATION
        ========================

        Country: %s
        Start date: %s
        End date: %s
        Total days: %d

        General preferences:
        %s

        Activity preferences:
        %s

        ========================
        CITY SELECTION RULES
        ========================

        Choose cities intelligently based on:

        1. The traveler's activity preferences and their priority.
        2. The total number of available trip days.
        3. Geographic proximity between the selected cities.
        4. A logical travel route between the cities.
        5. The traveler's pace and general preferences.
        6. The amount of meaningful activities each city can offer.

        Activity priorities MUST materially affect city selection.

        A higher-priority activity must influence the selected cities
        more strongly than a lower-priority activity.

        For example, if culture has a higher priority than shopping,
        cities with stronger cultural experiences should receive more
        consideration than cities mainly selected for shopping.

        Do NOT choose cities only because they are famous.

        Every selected city must provide meaningful value based on
        THIS traveler's preferences.

        Do not give generic recommendations that could apply to any traveler.

        ========================
        GEOGRAPHIC ROUTE RULES
        ========================

        Treat all selected cities as ONE connected travel route.

        Do NOT choose each city independently.

        Selected cities should be geographically reasonable together.

        Prefer cities that form a natural and efficient route.

        Avoid selecting one city that is significantly farther away
        from all the other selected cities.

        Avoid unnecessary long transfers.

        Do NOT create a route where two cities are reasonably close
        but another city is geographically isolated from the route.

        A city may strongly match the traveler's interests,
        but it should NOT be selected if including it makes
        the overall route unreasonable.

        Before returning the result:
        REVIEW the entire route yourself.

        Ask yourself:
        - Do these cities make sense together?
        - Is the visiting order logical?
        - Is one city unnecessarily far from the rest?
        - Would replacing one city create a smoother route
          without sacrificing the traveler's main interests?

        If one city makes the route geographically inefficient,
        replace it with a closer alternative that still matches
        the traveler's preferences.

        Exact driving time is NOT required.

        Do NOT estimate exact driving times, traffic conditions,
        or transportation durations.

        Do NOT rely on assumptions about traffic, car availability,
        or exact transportation schedules.

        Use reasonable geographic travel knowledge only.

        ========================
        NUMBER OF CITIES
        ========================

        Decide the appropriate number of cities based on trip duration.

        Do NOT add multiple cities unnecessarily.

        A short trip may contain only one city if that creates
        the best travel experience.

        Do NOT force a multi-city route when the trip duration
        does not reasonably support it.

        Avoid changing cities too frequently.

        Each selected city must receive enough time to be meaningfully visited.

        The goal is NOT to visit the maximum number of cities.

        The goal is to create the best overall travel experience
        within the available time.

        ========================
        DAY ALLOCATION
        ========================

        Do NOT divide the days equally by default.

        Allocate more days to cities that:
        - strongly match high-priority activities
        - contain more relevant experiences
        - reasonably deserve more time

        Allocate fewer days to cities that need less time.

        The complete trip date range MUST be covered.

        There must be:
        - no missing dates
        - no overlapping dates
        - no dates outside the trip range

        startDate and endDate are inclusive.

        cityOrder must begin at 1 and increase sequentially.

        ========================
        REASON STYLE
        ========================

        The "reason" for each city should use a warm,
        friendly, travel-oriented tone.

        It should feel helpful and inspiring,
        not robotic, formal, academic, or corporate.

        Speak naturally to the traveler.

        Explain WHY this city fits THIS traveler specifically.

        Connect the reason to:
        - the traveler's activity preferences
        - activity priorities
        - travel pace
        - the role of the city in the overall route

        Keep every reason concise and easy to read.

        Avoid generic phrases such as:
        "this city has many attractions"
        "this is a famous destination"
        "this city is popular with tourists"

        Avoid exaggerated marketing language.

        Do NOT make unsupported claims.

        Do NOT mention exact travel times.

        Avoid mentioning a specific train, flight, bus,
        or transportation service unless it is truly necessary.

        Prefer natural wording such as:
        "This makes a logical next stop in your route"
        instead of making specific transportation claims.

        Good reason style example:

        "Kyoto is a great second stop for you after Tokyo,
        adding a more traditional and cultural side to the trip
        while keeping the route smooth and easy to enjoy."

        Bad reason style example:

        "Kyoto is a famous tourist destination with many attractions."

        ========================
        IMPORTANT
        ========================

        Every city MUST be inside the provided country.

        Never suggest a city from another country.

        Do not invent additional trip dates.

        Do not return cities that do not meaningfully fit the traveler.

        Review the COMPLETE proposed route before producing the final answer.

        Return ONLY valid JSON.

        Do NOT return markdown.
        Do NOT return explanations outside the JSON.
        Do NOT include text before or after the JSON.

        JSON format:

        {
          "cities": [
            {
              "city": "City Name",
              "startDate": "YYYY-MM-DD",
              "endDate": "YYYY-MM-DD",
              "cityOrder": 1,
              "reason": "Friendly, specific reason based on the traveler's preferences and route logic."
            }
          ]
        }

        Final check before returning:

        1. All cities are inside the requested country.
        2. The route is geographically logical.
        3. No city is unnecessarily isolated from the route.
        4. Higher-priority activities influenced city selection.
        5. The number of cities fits the trip duration.
        6. All trip dates are covered exactly once.
        7. cityOrder is sequential.
        8. Reasons are friendly, specific, and personalized.
        9. No exact transportation time or unsupported transportation claim is included.

        Return only the final optimized city plan.
        """.formatted(
                outputLanguage,
                outputLanguage,
                trip.getCountry(),
                travelRequest.getStartDate(),
                travelRequest.getEndDate(),
                totalDays,
                travelRequest.getGeneralPreference(),
                travelRequest.getActivityPreferences()
        );


        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseMimeType", "application/json");


        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);


        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));


        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));
        requestBody.put("generationConfig", generationConfig);


        Map response = restClient.post()
                .uri("/v1beta/models/gemini-3.5-flash-lite:generateContent?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);


        if (response == null) {
            throw new ApiException("failed to generate city plan");
        }


        try {

            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                throw new ApiException("AI did not return city plan");
            }


            Map<String, Object> candidate = candidates.get(0);

            Map<String, Object> responseContent =
                    (Map<String, Object>) candidate.get("content");

            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>) responseContent.get("parts");

            String json =
                    (String) parts.get(0).get("text");


            return objectMapper.readValue(
                    json,
                    CityPlanDTO.class
            );

        } catch (Exception e) {

            throw new ApiException(
                    "failed to parse AI city plan"
            );
        }
    }
}