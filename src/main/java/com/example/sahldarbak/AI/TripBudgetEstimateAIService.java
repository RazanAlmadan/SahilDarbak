package com.example.sahldarbak.AI;


import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.BudgetEstimateRequestDTO;
import com.example.sahldarbak.DTO.TripBudgetEstimateDTO;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Model.Trip;
import com.example.sahldarbak.Model.TripCity;
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
public class TripBudgetEstimateAIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();


    // GENERATE TRIP BUDGET ESTIMATE
    public TripBudgetEstimateDTO generateBudgetEstimate(
            Trip trip,
            TravelRequest travelRequest,
            BudgetEstimateRequestDTO requestDTO,
            List<TripCity> tripCities,String lang) {
        String outputLanguage =
                "en".equalsIgnoreCase(lang)
                        ? "English"
                        : "Arabic";

        try {

            String tripContext =
                    objectMapper.writeValueAsString(
                            createBudgetContext(
                                    trip,
                                    travelRequest,
                                    requestDTO,
                                    tripCities
                            )
                    );


            String prompt = """
                    You are the friendly travel budget assistant for SahlDarbak.

                    Your job is to create a realistic PLANNING ESTIMATE for
                    the complete trip based on the supplied trip information.

                    The traveler should feel that a helpful travel companion
                    is explaining the expected budget, not a financial report.
                    
                    ========================
                    OUTPUT LANGUAGE
                    ========================
                    
                    The selected UI language is: %s
                    
                    Write ONLY the "summary" value in %s.
                    
                    Do NOT translate or modify:
                    - JSON keys
                    - currency codes
                    - numeric values
                    - city/country proper names
                    
                    Do not change the required JSON structure.

                    ========================
                    IMPORTANT ESTIMATE RULES
                    ========================
                    
                    1. All monetary values MUST be estimated in the currency
                       supplied in the trip information.
                       
                    2. The supplied currency represents BOTH:
                        - the currency of the user's targetBudget
                        - the currency that MUST be used for all generated estimates.
                    
                    3. Never interpret targetBudget using a different currency.
                    
                    4. Use the SAME supplied currency for:
                       - flightEstimate
                       - accommodationEstimate
                       - foodEstimate
                       - transportationEstimate
                       - activitiesEstimate
                    
                    5. Never change the requested currency.
                    
                    6. Do not perform or claim a live exchange-rate conversion.
                       These are approximate travel planning estimates only.


                    7. These are planning estimates only.

                    8. NEVER claim that any value is:
                       - a live price
                       - a guaranteed price
                       - a booking price
                       - an exact fare
                       - a final cost

                    9. Do not invent airlines, hotels, tickets,
                       transportation companies, offers or bookings.

                    10. Use reasonable general travel-cost knowledge
                       to estimate the trip.

                    11. All estimates must represent the TOTAL cost
                       for the entire traveling party, NOT per person.

                    12. Take the number of travelers and children into account.

                    13. Take trip duration and number of nights into account.

                    14. Take single-city versus multi-city travel into account.

                    15. The user's budget is a TARGET provided for comparison.

                    Do NOT force the estimated total to equal the user's budget.

                    ========================
                    FLIGHT ESTIMATE
                    ========================

                    flightEstimate should represent an estimated round-trip
                    flight cost for the entire traveling party.

                    For a single-city trip:
                    estimate travel from the supplied departure location
                    to the destination city and back.

                    For a multi-city trip:
                    consider travel from the supplied departure location
                    to the first city, then returning from the final city
                    back to the departure location.

                    Do NOT include local transportation or normal travel
                    between cities inside flightEstimate.

                    If departure city and destination city are the same,
                    flightEstimate may reasonably be 0.

                    ========================
                    ACCOMMODATION ESTIMATE
                    ========================

                    accommodationEstimate should represent the estimated
                    accommodation cost for all trip nights.

                    Consider:
                    - number of nights
                    - number of travelers
                    - family/group size
                    - number of cities
                    - general trip style and pace

                    ========================
                    FOOD ESTIMATE
                    ========================

                    foodEstimate should represent the estimated total food
                    cost for the entire traveling party for the whole trip.

                    Consider:
                    - number of travelers
                    - children when present
                    - trip duration
                    - food preferences when relevant
                    Food preferences may be used to estimate likely food spending,
                    but they do NOT prove that compliant restaurants or food options
                    are available at the destination.
                    
                    Never claim or imply in the summary that halal food, seafood-free food,
                    allergy-safe food, or any other restricted food option is available
                    or verified unless verified availability data was explicitly supplied.
                    
                    When discussing food in the summary, use neutral wording such as
                    "food expenses", "meal costs" or "your food preferences".

                    ========================
                    TRANSPORTATION ESTIMATE
                    ========================

                    transportationEstimate should include reasonable estimated
                    transportation costs during the trip.

                    This may include:
                    - local transportation
                    - travel inside each city
                    - reasonable movement between selected cities

                    Do NOT include the main round-trip flight here.

                    For multi-city trips, transportationEstimate should reflect
                    the additional cost of moving between cities.

                    Do not claim an exact transportation method or fare.

                    ========================
                    ACTIVITIES ESTIMATE
                    ========================

                    activitiesEstimate should reflect likely spending on
                    activities and attractions for the whole traveling party.

                    Consider:
                    - activity preferences
                    - activity priorities
                    - trip pace
                    - number of travel days
                    - number of travelers

                    ========================
                    SUMMARY STYLE
                    ========================

                    The summary MUST:

                    - be friendly and travel-oriented
                    - be concise
                    - feel encouraging and useful
                    - explain where the largest expected spending is
                    - compare the estimate with the user's target budget
                      when a budget is supplied
                    - clearly remind the traveler that costs are estimated

                    If the estimated trip appears above the user's budget,
                    explain it gently and suggest that the traveler may need
                    to adjust areas such as accommodation, activities or pace.

                    If it appears comfortably within budget, mention that
                    naturally without claiming prices are guaranteed.

                    Do NOT sound robotic, formal or alarming.

                    Example tone:

                    "Your trip looks fairly balanced overall. Accommodation
                    is likely to take the biggest share of the budget, while
                    you should still have comfortable room for local food
                    and experiences. Keep in mind that these are planning
                    estimates and actual travel prices can change."

                    ========================
                    OUTPUT RULES
                    ========================

                    Return ONLY valid JSON.

                    Do NOT return Markdown.
                    Do NOT return explanations outside the JSON.
                    Do NOT wrap the response in ```.

                    Return exactly this structure:

                    {
                      "flightEstimate": 0.0,
                      "accommodationEstimate": 0.0,
                      "foodEstimate": 0.0,
                      "transportationEstimate": 0.0,
                      "activitiesEstimate": 0.0,
                      "summary": "Friendly personalized budget summary"
                    }

                    IMPORTANT:

                    The 0.0 values above only show the required JSON structure.
                    Replace them with reasonable estimated values.

                    Do not return negative values.

                    TRIP INFORMATION:
                    %s
                    """.formatted(
                    outputLanguage,
                    outputLanguage,
                    tripContext
            );


            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put(
                    "responseMimeType",
                    "application/json"
            );


            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
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


            Map response = restClient.post()
                    .uri("/v1beta/models/gemini-3.5-flash-lite:generateContent")
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);


            if (response == null) {
                throw new ApiException(
                        "failed to generate trip budget estimate"
                );
            }


            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>)
                            response.get("candidates");


            if (candidates == null || candidates.isEmpty()) {
                throw new ApiException(
                        "AI did not generate a budget estimate"
                );
            }


            Map<String, Object> content =
                    (Map<String, Object>)
                            candidates.get(0).get("content");


            if (content == null) {
                throw new ApiException(
                        "invalid AI budget response"
                );
            }


            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>)
                            content.get("parts");


            if (parts == null || parts.isEmpty()) {
                throw new ApiException(
                        "invalid AI budget response"
                );
            }


            String json =
                    (String) parts.get(0).get("text");


            return objectMapper.readValue(
                    json,
                    TripBudgetEstimateDTO.class
            );


        } catch (ApiException e) {

            throw e;

        } catch (Exception e) {

            e.printStackTrace();

            throw new ApiException(
                    "failed to process trip budget estimate: "
                            + e.getMessage()
            );
        }
    }


    // CREATE ONLY THE TRIP DATA NEEDED BY AI
    private Map<String, Object> createBudgetContext(
            Trip trip,
            TravelRequest travelRequest,
            BudgetEstimateRequestDTO requestDTO,
            List<TripCity> tripCities) {

        Map<String, Object> context = new HashMap<>();


        // DEPARTURE
        context.put(
                "departureCountry",
                requestDTO.getDepartureCountry()
        );

        context.put(
                "departureCity",
                requestDTO.getDepartureCity()
        );


        // DESTINATION
        context.put(
                "destinationCountry",
                trip.getCountry()
        );

        context.put(
                "cityPlanMode",
                travelRequest.getCityPlanMode()
        );


        // DATES
        context.put(
                "startDate",
                travelRequest.getStartDate()
        );

        context.put(
                "endDate",
                travelRequest.getEndDate()
        );


        long tripDays =
                ChronoUnit.DAYS.between(
                        travelRequest.getStartDate(),
                        travelRequest.getEndDate()
                ) + 1;


        long nights =
                ChronoUnit.DAYS.between(
                        travelRequest.getStartDate(),
                        travelRequest.getEndDate()
                );


        context.put("tripDays", tripDays);
        context.put("nights", nights);


        // USER TARGET BUDGET
        context.put(
                "targetBudget",
                travelRequest.getBudget()
        );

        context.put(
                "currency",
                requestDTO.getCurrency()
        );


        // TRAVELERS
        context.put(
                "travelType",
                travelRequest.getTravelType()
        );

        context.put(
                "groupSize",
                travelRequest.getGroupSize()
        );

        context.put(
                "adultsCount",
                travelRequest.getAdultsCount()
        );


        int childrenCount = 0;

        if (travelRequest.getChildren() != null) {

            childrenCount =
                    travelRequest.getChildren().size();

            context.put(
                    "childrenAges",
                    travelRequest.getChildren()
                            .stream()
                            .map(child -> child.getAge())
                            .toList()
            );
        }

        context.put(
                "childrenCount",
                childrenCount
        );

        int totalTravelers;

        if (travelRequest.getTravelType().equals("solo")) {

            totalTravelers = 1;

        } else if (travelRequest.getTravelType().equals("couple")) {

            totalTravelers = 2;

        } else if (travelRequest.getTravelType().equals("group")) {

            totalTravelers = travelRequest.getGroupSize();

        } else {

            totalTravelers = travelRequest.getAdultsCount() + childrenCount;
        }

        context.put("totalTravelers", totalTravelers);

        // GENERAL PREFERENCES
        if (travelRequest.getGeneralPreference() != null) {

            Map<String, Object> generalPreference =
                    new HashMap<>();

            generalPreference.put(
                    "environment",
                    travelRequest.getGeneralPreference()
                            .getEnvironment()
            );

            generalPreference.put(
                    "tripPace",
                    travelRequest.getGeneralPreference()
                            .getTripPace()
            );

            generalPreference.put(
                    "crowdPreference",
                    travelRequest.getGeneralPreference()
                            .getCrowdPreference()
            );

            context.put(
                    "generalPreference",
                    generalPreference
            );
        }


        // ACTIVITY PREFERENCES
        if (travelRequest.getActivityPreferences() != null) {

            context.put(
                    "activityPreferences",
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
                            .toList()
            );
        }


        // FOOD PREFERENCES
        if (travelRequest.getFoodPreferences() != null) {

            context.put(
                    "foodPreferences",
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
                            .toList()
            );
        }


        // SINGLE CITY
        if (travelRequest.getCityPlanMode()
                .equals("single_city")) {

            context.put(
                    "cities",
                    List.of(
                            Map.of(
                                    "city",
                                    trip.getCity(),
                                    "startDate",
                                    travelRequest.getStartDate(),
                                    "endDate",
                                    travelRequest.getEndDate()
                            )
                    )
            );
        }


        // MULTI CITY
        else if (tripCities != null
                && !tripCities.isEmpty()) {

            List<Map<String, Object>> cities =
                    tripCities.stream()
                            .map(city -> {

                                Map<String, Object> cityMap =
                                        new HashMap<>();

                                cityMap.put(
                                        "city",
                                        city.getCity()
                                );

                                cityMap.put(
                                        "startDate",
                                        city.getStartDate()
                                );

                                cityMap.put(
                                        "endDate",
                                        city.getEndDate()
                                );

                                cityMap.put(
                                        "cityOrder",
                                        city.getCityOrder()
                                );

                                return cityMap;

                            })
                            .toList();


            context.put(
                    "cities",
                    cities
            );
        }


        return context;
    }
}