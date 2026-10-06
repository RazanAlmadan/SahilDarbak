package com.example.sahldarbak.AI;


import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.SmartItinerary.PlaceOptionDTO;
import com.example.sahldarbak.DTO.SmartItinerary.SmartItineraryDTO;
import com.example.sahldarbak.Model.TravelRequest;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SmartItineraryAIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();


    // GENERATE SMART ITINERARY
    public SmartItineraryDTO generateSmartItinerary(
            TravelRequest travelRequest,
            String city,
            LocalDate startDate,
            LocalDate endDate,
            List<PlaceOptionDTO> hotels,
            List<PlaceOptionDTO> activities,
            List<PlaceOptionDTO> restaurants) {

        try {

            String prompt = buildPrompt(
                    travelRequest,
                    city,
                    startDate,
                    endDate,
                    hotels,
                    activities,
                    restaurants
            );

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseMimeType", "application/json");

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    ),
                    "generationConfig", generationConfig
            );


            Map response = restClient.post()

                    .uri("/v1beta/models/gemini-3.5-flash-lite:generateContent")

                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);


            if (response == null) {
                throw new ApiException("failed to generate smart itinerary");
            }


            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                throw new ApiException("AI did not generate an itinerary");
            }


            Map<String, Object> content =
                    (Map<String, Object>) candidates.get(0).get("content");

            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                throw new ApiException("invalid AI response");
            }


            String json =
                    (String) parts.get(0).get("text");


            return objectMapper.readValue(
                    json,
                    SmartItineraryDTO.class
            );


        } catch (ApiException e) {
            throw e;

        } catch (Exception e) {
            e.printStackTrace();

            throw new ApiException(
                    "failed to process smart itinerary: " + e.getMessage()
            );
        }
    }


    // BUILD AI PROMPT
    private String buildPrompt(
            TravelRequest travelRequest,
            String city,
            LocalDate startDate,
            LocalDate endDate,
            List<PlaceOptionDTO> hotels,
            List<PlaceOptionDTO> activities,
            List<PlaceOptionDTO> restaurants) throws Exception {


        String travelRequestJson = objectMapper.writeValueAsString(
                        createTravelRequestContext(
                                travelRequest,
                                city,
                                startDate,
                                endDate));

        String hotelsJson = objectMapper.writeValueAsString(hotels);

        String activitiesJson = objectMapper.writeValueAsString(activities);

        String restaurantsJson = objectMapper.writeValueAsString(restaurants);


        return """
        You are the smart itinerary planner for SahlDarbak.

        Your job is to create a realistic, personalized and high-quality
        travel itinerary using the user's travel request, preferences,
        restrictions and ONLY the real candidate places provided below.

        You MUST follow all rules below strictly.

        =========================
        CORE DATA RULES
        =========================

        1. NEVER invent a hotel, restaurant, activity, externalId,
           rating, review count, price, opening hour, halal status,
           source, URL, distance, address, menu content, crowd level
           or any other factual information.

        2. Every selected place MUST exist in the provided candidate data.

        3. Every selected place MUST use EXACTLY the same:
           - externalId
           - name
           - factual data
           provided in the selected candidate.

        4. NEVER select the same externalId more than once anywhere
           in the entire response.

        5. Duplicate checking applies across:
           - days[].places
           - hotelRecommendations
           - restaurantRecommendations
           - activityRecommendations

        6. Avoid selecting different candidate records that clearly
           represent the same physical place or landmark even if
           their externalIds are different.

        =========================
        CANDIDATE DATA COPYING
        =========================

        7. The candidate objects may contain:

           - ratings
           - estimatedPrice
           - halalInfo
           - websiteUrl

        8. For every selected place, COPY candidate data EXACTLY.

        9. NEVER calculate, rewrite, summarize, modify or infer:

           - ratings
           - rating values
           - maxRating
           - reviewCount
           - rating source
           - rating sourceUrl
           - estimatedPrice
           - estimatedPricePerNight
           - currency
           - price source
           - price sourceUrl
           - halalInfo
           - halalStatus
           - halal evidence
           - halal source
           - halal sourceUrl
           - websiteUrl

        10. Map candidate websiteUrl to output field:

            officialWebsite

        11. If candidate websiteUrl is null,
            officialWebsite MUST be JSON null.

        12. If ratings are null in the candidate,
            output ratings as JSON null.

        13. If estimatedPrice is null in the candidate,
            output estimatedPrice as JSON null.

        14. If halalInfo is null in the candidate,
            output halalInfo as JSON null.

        15. NEVER create a URL that is not already supplied
            in the candidate data.

        =========================
        TRAVEL DATE RULES
        =========================

        16. Create exactly one itinerary day for EVERY date from
            startDate through endDate, inclusive.

        17. Do not skip any travel date.

        18. Dates must be ordered chronologically.

        19. dayNumber must begin at 1 and increase sequentially.

        =========================
        USER PREFERENCES
        =========================

        20. REQUIRED food preferences and REQUIRED travel restrictions
            are HARD CONSTRAINTS.

        21. Never recommend a place that is known to violate
            a required restriction.

        22. If compliance with a required restriction cannot be verified
            from the supplied candidate data, NEVER claim that the place
            satisfies that restriction.

        =========================
        HALAL RULES
        =========================

        23. halalInfo.halalStatus can contain ONLY these supplied states:

            - FULLY_HALAL
            - PARTIAL_HALAL
            - NOT_VERIFIED

        24. FULLY_HALAL means the supplied candidate data contains
            strong evidence that the restaurant is fully halal.

        25. PARTIAL_HALAL means only partial halal availability,
            halal options, or a halal menu has been verified.

        26. NOT_VERIFIED means the supplied data does not provide
            enough evidence to verify halal status.

        27. If halal food is REQUIRED:

            - ONLY restaurants with:
              halalInfo.halalStatus = "FULLY_HALAL"
              may be selected.

            - PARTIAL_HALAL restaurants MUST NOT be selected.

            - NOT_VERIFIED restaurants MUST NOT be selected.

            - This applies to:
              days[].places
              AND restaurantRecommendations.

            - If no FULLY_HALAL restaurant is available,
              leave the meal slot without a restaurant rather than
              selecting PARTIAL_HALAL or NOT_VERIFIED.

        28. If halal food is NOT required:

            - Restaurants may be selected regardless of halal status.

            - However, halalInfo MUST still be copied exactly
              from candidate data.

            - Never describe PARTIAL_HALAL as fully halal.

            - Never describe NOT_VERIFIED as halal.

        29. Never infer halal status from:

            - restaurant name
            - cuisine type
            - destination country
            - vegetarian food
            - absence of seafood
            - culture or religion
            - any indirect clue

        30. halalInfo.evidence and halalInfo.source may only be used
            as supporting facts.

        31. Avoiding seafood DOES NOT mean a restaurant is halal.

        =========================
        DIETARY RESTRICTIONS
        =========================

        32. Dietary restrictions are independent from halal status.

        33. halalInfo verifies halal status ONLY.

            It does NOT verify:
            - seafood-free status
            - allergy safety
            - vegetarian status
            - menu contents
            - other dietary restrictions

        34. Never claim that a restaurant avoids seafood or satisfies
            another dietary restriction unless the supplied candidate
            data explicitly verifies that fact.

        35. Never infer dietary compliance, menu contents,
            crowd level, popularity, quality or accessibility
            from a place name or category.

        =========================
        ACTIVITY SELECTION
        =========================

        36. Activity preference priority MUST materially affect selection.

        37. Prefer higher priority activity types over lower priority types.

        38. Priority 5 must be favored over priority 4,
            and priority 4 over priority 2,
            whenever suitable candidates exist.

        39. Prefer meaningful attractions such as:

            - museums
            - major cultural attractions
            - historical sites
            - significant landmarks
            - important shopping districts
            - major entertainment attractions

        40. Avoid minor statues, decorative objects,
            insignificant POIs and trivial landmarks when stronger
            candidates are available.

        41. Do not revisit an attraction simply to fill a day.

        =========================
        DAILY PLANNING
        =========================

        42. Respect the user's tripPace.

        43. For a moderate trip pace, normally schedule approximately
            two meaningful activities per full day with reasonable
            time for meals, walking, transportation and rest.

        44. Arrange places geographically when latitude, longitude
            or distance data makes this possible.

        45. Avoid unrealistic back-and-forth movement.

        46. Restaurants should be placed naturally around meal times.

        47. suggestedTime values inside days[].places must be realistic
            and chronologically ordered.

        =========================
        HOTELS
        =========================

        48. Recommend a maximum of 5 hotels.

        49. Hotel recommendations are informational only.

        50. ratings MUST be copied EXACTLY from the selected
            hotel candidate.

        51. estimatedPrice MUST be copied EXACTLY from the selected
            hotel candidate.

        52. Never create a rating or review count.

        53. Never create or calculate a hotel price.

        54. Hotel price information is ESTIMATED only.

        55. When describing an estimated hotel price:

            - mention that it is estimated
            - use the supplied currency
            - never describe it as final or guaranteed

        56. NEVER compare a per-night hotel price directly
            with the total trip budget.

        57. NEVER claim that a hotel fits the user's total budget
            when currency conversion and total stay cost are not supplied.

        58. If estimatedPrice is null,
            do not make any price claim.

        59. Never create booking, reservation or payment information.

        60. Source URLs inside ratings or estimatedPrice MUST be copied
            exactly from candidate data.

        =========================
        RESTAURANT RECOMMENDATIONS
        =========================

        61. restaurantRecommendations contains useful alternatives
            that were NOT selected inside days[].places.

        62. A restaurant already used in the daily itinerary
            MUST NOT appear in restaurantRecommendations.

        63. Recommend a maximum of 5 additional restaurants.

        64. If halal is REQUIRED,
            restaurantRecommendations may contain ONLY:

            halalInfo.halalStatus = "FULLY_HALAL"

        65. halalInfo MUST be copied EXACTLY from the selected
            restaurant candidate.

        66. Never describe a restaurant as:
            - fully halal
            - highly rated
            - affordable
            - vegetarian-friendly
            - seafood-free
            - suitable for another restriction

            unless the supplied candidate data explicitly supports it.

        =========================
        ACTIVITY RECOMMENDATIONS
        =========================

        67. activityRecommendations contains useful alternative activities
            that match the user's preferences but were NOT selected
            in days[].places.

        68. An activity already used in the daily itinerary
            MUST NOT appear in activityRecommendations.

        69. Recommend a maximum of 5 additional activities.

        70. Rank additional activities according to the user's
            activity priorities.

        71. Additional activities are alternatives only.
            They are NOT scheduled parts of the itinerary.

        72. Do not add insignificant POIs merely to fill
            activityRecommendations.

        =========================
        REASONS
        =========================

        73. Every reason must explain WHY the selected place
            suits this traveler.

        74. Reasons may use ONLY supplied facts such as:

            - activity preference and priority
            - trip pace
            - environment preference
            - required restriction compatibility
            - supplied halal information
            - supplied ratings and review counts
            - supplied estimated hotel price
            - supplied location or proximity information
            - supplied place type

        75. Never invent supporting facts inside reason.

        76. Avoid vague reasons such as:

            "A great place to visit"
            "Explore local culture"
            "Convenient and enjoyable"

            unless the reason also refers to a specific supplied
            traveler preference or candidate fact.

        77. Never claim that a restaurant respects a dietary restriction
            unless supplied candidate data explicitly verifies it.

        78. Never say a place has low crowds, is quiet,
            popular, affordable, luxurious or highly rated
            unless supplied data explicitly proves it.

        =========================
        JSON NULL RULES
        =========================

        79. JSON null and the String "null" are NOT the same.

        80. NEVER return the text "null" inside quotation marks
            for any nullable field.

        81. When a field has no value, return the actual JSON null value.

        CORRECT:
        "suggestedTime": null

        WRONG:
        "suggestedTime": "null"

        CORRECT:
        "ratings": null

        WRONG:
        "ratings": "null"

        CORRECT:
        "estimatedPrice": null

        WRONG:
        "estimatedPrice": "null"

        CORRECT:
        "halalInfo": null

        WRONG:
        "halalInfo": "null"

        CORRECT:
        "officialWebsite": null

        WRONG:
        "officialWebsite": "null"

        82. For ALL items inside:

            - hotelRecommendations
            - restaurantRecommendations
            - activityRecommendations

            suggestedTime MUST ALWAYS be the actual JSON null value.

            It MUST be:

            "suggestedTime": null

            NEVER:

            "suggestedTime": "null"

        =========================
        OUTPUT VALIDATION
        =========================

        83. Before returning the response, internally verify:

            - every travel date appears exactly once
            - dates are chronological
            - dayNumber is sequential
            - no externalId is duplicated anywhere
            - no place is unnecessarily repeated
            - every selected externalId exists in candidate data
            - selected names exactly match candidate names
            - all required restrictions are respected
            - halal-required restaurants are FULLY_HALAL only
            - activity priorities influence selection
            - hotel ratings are copied exactly
            - hotel prices are copied exactly
            - halalInfo is copied exactly
            - source URLs are copied exactly
            - websiteUrl is copied into officialWebsite exactly
            - restaurantRecommendations do not duplicate daily restaurants
            - activityRecommendations do not duplicate daily activities
            - no unsupported facts are invented
            - NO nullable value is returned as the String "null"

        84. Return JSON ONLY.

        Do not return Markdown.
        Do not return explanation outside the JSON.
        Do not wrap the JSON in ```.

        =========================
        REQUIRED JSON STRUCTURE
        =========================

        The JSON MUST have exactly this structure:

        {
          "days": [
            {
              "dayNumber": 1,
              "date": "YYYY-MM-DD",
              "places": [
                {
                  "externalId": "exact candidate externalId",
                  "name": "exact candidate name",
                  "type": "activity or restaurant",
                  "suggestedTime": "HH:mm:ss",
                  "reason": "specific evidence-based reason",

                  "ratings": null,
                  "estimatedPrice": null,

                  "halalInfo": {
                    "halalStatus": "FULLY_HALAL",
                    "evidence": "exact candidate evidence",
                    "source": "exact candidate source",
                    "sourceUrl": "exact candidate sourceUrl"
                  },

                  "officialWebsite": "exact candidate websiteUrl or null"
                }
              ]
            }
          ],

          "hotelRecommendations": [
            {
              "externalId": "exact candidate externalId",
              "name": "exact candidate name",
              "type": "hotel",
              "suggestedTime": null,
              "reason": "specific evidence-based reason",

              "ratings": [
                {
                  "rating": 0.0,
                  "maxRating": 0.0,
                  "reviewCount": 0,
                  "source": "exact candidate source",
                  "sourceUrl": "exact candidate sourceUrl"
                }
              ],

              "estimatedPrice": {
                "estimatedPricePerNight": 0.0,
                "currency": "exact candidate currency",
                "source": "exact candidate source",
                "sourceUrl": "exact candidate sourceUrl"
              },

              "halalInfo": null,

              "officialWebsite": "exact candidate websiteUrl or null"
            }
          ],

          "restaurantRecommendations": [
            {
              "externalId": "exact candidate externalId",
              "name": "exact candidate name",
              "type": "restaurant",
              "suggestedTime": null,
              "reason": "specific evidence-based reason",

              "ratings": null,
              "estimatedPrice": null,

              "halalInfo": {
                "halalStatus": "FULLY_HALAL",
                "evidence": "exact candidate evidence",
                "source": "exact candidate source",
                "sourceUrl": "exact candidate sourceUrl"
              },

              "officialWebsite": "exact candidate websiteUrl or null"
            }
          ],

          "activityRecommendations": [
            {
              "externalId": "exact candidate externalId",
              "name": "exact candidate name",
              "type": "activity",
              "suggestedTime": null,
              "reason": "specific evidence-based reason",

              "ratings": null,
              "estimatedPrice": null,
              "halalInfo": null,

              "officialWebsite": "exact candidate websiteUrl or null"
            }
          ]
        }

        IMPORTANT:
        The example values 0.0 above are placeholders showing the JSON shape.
        NEVER output 0.0 unless the selected candidate actually contains 0.0.
        Copy the candidate values exactly.
        If candidate data is absent, use actual JSON null.

        TRAVEL REQUEST:
        %s

        REAL HOTEL CANDIDATES:
        %s

        REAL ACTIVITY CANDIDATES:
        %s

        REAL RESTAURANT CANDIDATES:
        %s
        """.formatted(
                travelRequestJson,
                hotelsJson,
                activitiesJson,
                restaurantsJson
        );
    }


    // CREATE ONLY THE TRAVEL DATA NEEDED BY AI
    private Map<String, Object> createTravelRequestContext(
            TravelRequest travelRequest,
            String city,
            LocalDate startDate,
            LocalDate endDate) {

        Map<String, Object> context = new HashMap<>();

        context.put("startDate", startDate);
        context.put("endDate", endDate);
        context.put("budget", travelRequest.getBudget());
        context.put("travelType", travelRequest.getTravelType());
        context.put("groupSize", travelRequest.getGroupSize());
        context.put("adultsCount", travelRequest.getAdultsCount());


        if (travelRequest.getTrip() != null) {

            context.put(
                    "country",
                    travelRequest.getTrip().getCountry()
            );

            context.put(
                    "city",city
            );
        }


        if (travelRequest.getGeneralPreference() != null) {

            Map<String, Object> generalPreference = new HashMap<>();

            generalPreference.put(
                    "weather",
                    travelRequest.getGeneralPreference().getWeather()
            );

            generalPreference.put(
                    "environment",
                    travelRequest.getGeneralPreference().getEnvironment()
            );

            generalPreference.put(
                    "crowdPreference",
                    travelRequest.getGeneralPreference().getCrowdPreference()
            );

            generalPreference.put(
                    "tripPace",
                    travelRequest.getGeneralPreference().getTripPace()
            );

            context.put(
                    "generalPreference",
                    generalPreference
            );
        }


        if (travelRequest.getFoodPreferences() != null) {

            List<Map<String, Object>> foodPreferences =
                    travelRequest.getFoodPreferences()
                            .stream()
                            .map(food -> {

                                Map<String, Object> foodMap =
                                        new HashMap<>();

                                foodMap.put(
                                        "foodType",
                                        food.getFoodType()
                                );

                                foodMap.put(
                                        "required",
                                        food.getIsRequired()
                                );

                                return foodMap;

                            })
                            .toList();

            context.put(
                    "foodPreferences",
                    foodPreferences
            );
        }


        if (travelRequest.getActivityPreferences() != null) {

            List<Map<String, Object>> activityPreferences =
                    travelRequest.getActivityPreferences()
                            .stream()
                            .map(activity -> {

                                Map<String, Object> activityMap =
                                        new HashMap<>();

                                activityMap.put(
                                        "activityType",
                                        activity.getActivityType()
                                );

                                activityMap.put(
                                        "priority",
                                        activity.getPriority()
                                );

                                return activityMap;

                            })
                            .toList();

            context.put(
                    "activityPreferences",
                    activityPreferences
            );
        }


        if (travelRequest.getTravelRestrictions() != null) {

            List<Map<String, Object>> restrictions =
                    travelRequest.getTravelRestrictions()
                            .stream()
                            .map(restriction -> {

                                Map<String, Object> restrictionMap =
                                        new HashMap<>();

                                restrictionMap.put(
                                        "restrictionType",
                                        restriction.getRestrictionType()
                                );

                                restrictionMap.put(
                                        "description",
                                        restriction.getDescription()
                                );

                                restrictionMap.put(
                                        "required",
                                        restriction.getIsRequired()
                                );

                                return restrictionMap;

                            })
                            .toList();

            context.put(
                    "travelRestrictions",
                    restrictions
            );
        }


        if (travelRequest.getChildren() != null) {

            List<Integer> childrenAges =
                    travelRequest.getChildren()
                            .stream()
                            .map(child -> child.getAge())
                            .toList();

            context.put(
                    "childrenAges",
                    childrenAges
            );
        }


        return context;
    }
}