package com.example.sahldarbak.AI;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.CountryComparisonDTO;
import com.example.sahldarbak.ExternalApi.HolidayService;
import com.example.sahldarbak.ExternalApi.WeatherService;
import com.example.sahldarbak.Model.*;
import com.example.sahldarbak.Repository.TravelRequestRepository;
import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AIDestinationRecommendationService {

    private final TravelRequestRepository travelRequestRepository;

    private final WeatherService weatherService;

    private final HolidayService holidayService;

    private final ObjectMapper objectMapper;


    @Value("${gemini.api.key}")
    private String apiKey;


    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();


    public AIRecommendationResponse generateCountry(Integer travel_request_id) {

        TravelRequest travelRequest = travelRequestRepository.findTravelRequestById(travel_request_id);


        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }

        if (!travelRequest.getStatus().equals("open")){
            throw new ApiException("you did not submit your request");
        }

        //Collect the user's preferences.
        String userPreferences = buildUserPreferences(travelRequest);



        // Ask Gemini to suggest candidate countries.
        String firstPrompt = """
                You are a travel recommendation assistant.

                Analyze the following user's travel preferences.

                Suggest exactly 5 suitable destination countries.

                For every country return:
                - country
                - countryCode
                - capital

                The countryCode must be the ISO 3166-1 alpha-2 code.

                Return ONLY valid JSON.

                Use exactly this format:

                {
                  "destinations": [
                    {
                      "country": "Japan",
                      "countryCode": "JP",
                      "capital": "Tokyo"
                    }
                  ]
                }

                USER PREFERENCES:
                """ + userPreferences;


        String candidateResponse = callGemini(firstPrompt);




        // Parse Gemini's countries.
        JsonObject candidateObject;

        try {

            candidateObject =
                    JsonParser.parseString(candidateResponse)
                            .getAsJsonObject();

        } catch (Exception e) {
            throw new ApiException("AI returned invalid JSON: " + candidateResponse);
        }

        JsonArray destinations = candidateObject.getAsJsonArray("destinations");

        if (destinations == null || destinations.size() == 0) {
            throw new ApiException("AI did not return any destination countries");
        }

         // Get weather + holidays for every country using the external API.

        JsonArray externalData = new JsonArray();

        for (JsonElement element : destinations) {

            JsonObject destination = element.getAsJsonObject();

            String country = destination.get("country").getAsString();

            String countryCode = destination.get("countryCode").getAsString();

            String capital = destination.get("capital").getAsString();

            JsonObject destinationData = new JsonObject();

            destinationData.addProperty("country", country);

            destinationData.addProperty("countryCode", countryCode);

            destinationData.addProperty("capital", capital);



            // Get weather using OpenWeather API

            try {

                JsonObject weather = weatherService.getWeather(capital);

                destinationData.add("weather", weather);

            } catch (Exception e) {
                destinationData.addProperty("weatherError", e.getMessage());
            }



            // Get holidays using Nager.date API
            try {

                JsonArray holidays = getHolidaysForTrip(countryCode, travelRequest.getStartDate(), travelRequest.getEndDate());

                destinationData.add("holidays", holidays);

            } catch (Exception e) {
                destinationData.addProperty("holidayError", e.getMessage());
            }
            externalData.add(destinationData);
        }



         // Give Gemini the preferences + weather + holidays.

        String finalPrompt = buildFinalPrompt(userPreferences, externalData);


        // Gemini analyzes everything and returns final JSON.
        String aiResponse = callGemini(finalPrompt);

        Gson gson = new Gson();

        AIRecommendationResponse response = gson.fromJson(aiResponse, AIRecommendationResponse.class);

        return response;
    }


    // collect all the user preferences in one string
    private String buildUserPreferences(TravelRequest travelRequest) {

        StringBuilder text = new StringBuilder();

        // Date of travel
        text.append("TRAVEL DATES:\n")
                .append("Start Date: ")
                .append(travelRequest.getStartDate())
                .append("\n")
                .append("End Date: ")
                .append(travelRequest.getEndDate())
                .append("\n\n");


        // User budget
        text.append("BUDGET:\n")
                .append(travelRequest.getBudget())
                .append("\n\n");


        // Travel Type
        text.append("TRAVEL TYPE:\n")
                .append(travelRequest.getTravelType())
                .append("\n");


        // if there was a group
        if (travelRequest.getGroupSize() != null) {
            text.append("Group Size: ")
                    .append(travelRequest.getGroupSize())
                    .append("\n");
        }


        if (travelRequest.getAdultsCount() != null) {

            text.append("Adults: ")
                    .append(travelRequest.getAdultsCount())
                    .append("\n");
        }


        text.append("\n");

        // General preference

        GeneralPreference g = travelRequest.getGeneralPreference();

        if (g != null) {

            text.append("GENERAL PREFERENCES:\n");

            text.append("Weather: ")
                    .append(g.getWeather())
                    .append("\n");

            text.append("Environment: ")
                    .append(g.getEnvironment())
                    .append("\n");

            text.append("Crowd Preference: ")
                    .append(g.getCrowdPreference())
                    .append("\n");

            text.append("Trip Pace: ")
                    .append(g.getTripPace())
                    .append("\n\n");
        }

        // Food preferences

        text.append("FOOD PREFERENCES:\n");

        if (travelRequest.getFoodPreferences() != null) {

            for (FoodPreference f :
                    travelRequest.getFoodPreferences()) {

                text.append("- Food Type: ")
                        .append(f.getFoodType())
                        .append("\n");

                text.append("  Required: ")
                        .append(f.getIsRequired())
                        .append("\n");
            }
        }


        text.append("\n");



        // Activity preferences

        text.append("ACTIVITY PREFERENCES:\n");

        if (travelRequest.getActivityPreferences() != null) {

            for (ActivityPreference a :
                    travelRequest.getActivityPreferences()) {

                text.append("- Activity: ")
                        .append(a.getActivityType())
                        .append("\n");

                text.append("  Priority: ")
                        .append(a.getPriority())
                        .append("\n");
            }
        }


        text.append("\n");


        // Travel restrictions

        text.append("TRAVEL RESTRICTIONS:\n");

        if (travelRequest.getTravelRestrictions() != null) {

            for (TravelRestriction r :
                    travelRequest.getTravelRestrictions()) {

                text.append("- Type: ")
                        .append(r.getRestrictionType())
                        .append("\n");

                text.append("  Description: ")
                        .append(r.getDescription())
                        .append("\n");

                text.append("  Required: ")
                        .append(r.getIsRequired())
                        .append("\n");
            }
        }


        return text.toString();
    }


    private JsonArray getHolidaysForTrip(String countryCode, LocalDate startDate, LocalDate endDate) {

        JsonArray result = new JsonArray();



        // Get holidays for the year in which the trip starts.

        JsonArray startYearHolidays = holidayService.getHolidays(countryCode, startDate.getYear());

        addRelevantHolidays(result, startYearHolidays, startDate, endDate);

        /*
         * If the trip crosses into another year,
         * also get holidays for the next year.
         */

        if (startDate.getYear() != endDate.getYear()) {

            JsonArray endYearHolidays = holidayService.getHolidays(countryCode, endDate.getYear());

            addRelevantHolidays(result, endYearHolidays, startDate, endDate);
        }

        return result;
    }


    private void addRelevantHolidays(JsonArray result, JsonArray holidays, LocalDate startDate, LocalDate endDate) {

        for (JsonElement element : holidays) {

            JsonObject holiday = element.getAsJsonObject();

            if (!holiday.has("date")) {
                continue;
            }


            LocalDate holidayDate = LocalDate.parse(holiday.get("date").getAsString());

            /*
             * Only add holidays that happen
             * during the user's trip.
             */

            if (!holidayDate.isBefore(startDate) && !holidayDate.isAfter(endDate)) {
                result.add(holiday);
            }
        }
    }


    private String buildFinalPrompt(
            String userPreferences,
            JsonArray externalData
    ) {

        return """
                You are the travel buddy behind Sahl Darbak.
                
                        Your personality is:
                        - Fun
                        - Excited
                        - Friendly
                        - Warm
                        - Conversational
                        - Encouraging
                        - Curious about travel
                        - Helpful without sounding like a formal travel agency
                
                        Talk to the user like an enthusiastic friend who LOVES discovering
                        new places and is helping them choose their next adventure.
                
                        Your recommendations should feel exciting and personal, not like
                        a technical report.
                
                        IMPORTANT:
                        The response is still consumed by a Java application, so you MUST
                        follow the JSON structure provided below exactly.
                
                        Do not sacrifice JSON correctness for personality.
                
                        ==============================
                        PERSONALITY AND TONE
                        ==============================
                
                        Use natural, exciting language.
                
                        GOOD:
                        "Kazakhstan looks like a fantastic match for your trip! The chilly
                        weather is right up your alley, and Astana gives you a great mix of
                        culture and unique landscapes."
                
                        GOOD:
                        "If you're craving crisp air and beautiful scenery, Norway could
                        definitely be your vibe!"
                
                        GOOD:
                        "A little heads-up: your trip overlaps with a public holiday,
                        so popular spots could get noticeably busier. If you want a
                        calmer experience, booking ahead is a smart move."
                
                        BAD:
                        "The destination demonstrates high compatibility with the user's
                        environmental and climatic preferences."
                
                        BAD:
                        "Moderate crowd levels are expected based on available data."
                
                        BAD:
                        "The weather conditions are suitable for the requested activities."
                
                        Avoid corporate, academic, robotic, or overly formal language.
                
                        Do NOT repeatedly use phrases such as:
                        - "aligns with your preference"
                        - "satisfies your preference"
                        - "demonstrates suitability"
                        - "is suitable for"
                        - "moderate crowd levels are expected"
                        - "the destination provides"
                        - "the destination offers"
                        - "based on the available data"
                
                        Instead, speak naturally and make the recommendation feel like
                        a real travel conversation.
                
                        You may use exclamation marks when appropriate, but do not
                        overuse them.
                
                        You may use a small number of emojis in TEXT VALUES if they make
                        the recommendation more fun. Keep them tasteful and relevant,
                        such as:
                        🌍 ✈️ 🏔️ 🌊 ☀️ ❄️ 🍜 🎉
                
                        Do not put emojis in countryCode, rank, score, dates, or other
                        machine-readable values.
                
                        ==============================
                        YOUR JOB
                        ==============================

                Your task is to analyze the user's travel preferences,
                weather information, and public holiday information.

                ==============================
                USER PREFERENCES
                ==============================

                %s


                ==============================
                EXTERNAL API DATA
                ==============================

                The following data was retrieved from real external APIs.

                WEATHER DATA:
                OpenWeather API

                HOLIDAY DATA:
                Nager.Date API

                DATA:
                %s


                ==============================
                IMPORTANT RULES
                ==============================

                1. Never invent weather information.

                2. Never invent public holidays.

                3. Use ONLY the weather information provided by
                   OpenWeather.

                4. Use ONLY the holiday information provided by
                   Nager.Date.

                5. Analyze the destinations according to the user's
                   preferences.

                6. Consider the user's:
                   - weather preference
                   - environment preference
                   - crowd preference
                   - trip pace
                   - food preferences
                   - activity preferences
                   - restrictions
                   - budget
                   - travel dates
                   - travel type

                7. Determine whether the weather is suitable for
                   the user's trip and preferred activities.

                8. Determine whether the trip overlaps with public
                   holidays.

                9. Use public holidays to estimate possible crowd
                   levels.

                10. Crowd level must be one of:
                    LOW
                    MEDIUM
                    HIGH

                11. Crowd level is only an estimate.
                    Do not claim that the API measured actual
                    tourist crowds.

                12. If a public holiday occurs during the trip,
                    provide a clear heads-up to the user.

                13. Rank destinations from most suitable to least
                    suitable.

                14. Give every destination a suitability score
                    between 0 and 100.

                15. The score should represent how suitable the
                    destination is for THIS USER.

                ==============================
                RECOMMENDATION LEVELS
                ==============================

                90-100 = HIGHLY_RECOMMENDED

                75-89 = RECOMMENDED

                50-74 = CONSIDER

                0-49 = NOT_RECOMMENDED


                ==============================
                REQUIRED JSON RESPONSE
                ==============================

                Your ENTIRE response MUST be valid JSON.

                Do NOT return Markdown.

                Do NOT use ```json.

                Do NOT write anything before or after the JSON.

                Return exactly this structure:

                {
                  "status": "success",
                  "travelSummary": {
                    "destinationCount": 0,
                    "overallRecommendation": "",
                    "generalAdvice": ""
                  },
                  "destinations": [
                    {
                      "country": "",
                      "countryCode": "",
                      "capital": "",
                      "rank": 1,
                      "suitabilityScore": 0,
                      "recommendation": "RECOMMENDED",

                      "weather": {
                        "condition": "",
                        "temperatureCelsius": 0,
                        "suitableForTrip": true,
                        "explanation": ""
                      },

                      "holidays": {
                        "hasHolidayDuringTrip": false,
                        "holidayCount": 0,
                        "holidays": [
                          {
                            "name": "",
                            "date": "",
                            "type": ""
                          }
                        ]
                      },

                      "crowd": {
                        "level": "LOW",
                        "risk": "",
                        "reason": ""
                      },

                      "headsUp": "",

                      "whyItMatches": [
                        "",
                        "",
                        ""
                      ]
                    }
                  ]
                }


                ==============================
                FIELD RULES
                ==============================

                status:
                Must be "success".

                destinationCount:
                Must equal the number of destinations.

                suitabilityScore:
                Must be between 0 and 100.

                recommendation:
                Must be one of:

                HIGHLY_RECOMMENDED
                RECOMMENDED
                CONSIDER
                NOT_RECOMMENDED

                weather.suitableForTrip:
                Must be true or false.

                holidays.hasHolidayDuringTrip:
                Must be true if at least one public holiday
                occurs during the travel dates.

                holidays.holidayCount:
                Must equal the number of holidays in the array.

                crowd.risk:
                Use friendly and natural language.
                
                GOOD:
                "Looks like a pretty comfortable crowd situation — you shouldn't have
                to fight through huge crowds everywhere."
                
                GOOD:
                "Things could get lively! The holiday period may bring more visitors
                to popular attractions."
                
                GOOD:
                "Nice and chill! There are no major holiday dates during your trip,
                so there isn't an obvious reason to expect a crowd spike."
                
                Avoid formal phrases such as:
                "Moderate crowd level estimated."
                "Expected tourist activity."
                "Based on general estimates."

                headsUp:
                This should feel like a friendly heads-up from a travel buddy.
                
                If there is a holiday:
                
                "🎉 Heads-up! Your trip overlaps with a public holiday on October 15,
                so popular attractions could be busier than usual. Booking ahead would
                be a good idea!"
                
                If there is no holiday:
                
                "No big holiday crowds expected during your trip! 🙌"

                whyItMatches:
                Must contain exactly 3 short, fun, conversational reasons.
                
                These should feel like exciting travel suggestions rather than a checklist.
                
                GOOD:
                [
                  "❄️ The chilly weather is exactly your kind of adventure.",
                  "🏔️ You’ll get amazing mountain scenery and plenty of nature to explore.",
                  "🍜 Finding halal food should be pretty easy here."
                ]
                
                BAD:
                [
                  "The weather aligns with the user's preference.",
                  "The destination provides natural environments.",
                  "Halal food availability satisfies the dietary requirement."
                ]
                overallRecommendation:
                Write this like a travel buddy giving the user their top recommendation.
                
                Make it exciting and personal.
                
                GOOD:
                "Canada looks like your winner! ❄️ You’ll get the chilly weather you love,
                beautiful nature, and plenty of culture without giving up your relaxed pace."
                
                BAD:
                "Canada and Kazakhstan are the most suitable destinations based on the
                user's preferences."
                
                generalAdvice:
                Give friendly, useful travel advice in a conversational tone.
                
                GOOD:
                "Pack your warmest layers! 🧥 October can get seriously chilly, especially
                in Kazakhstan and Canada. And since you have a nut allergy, it’s worth
                double-checking ingredients before digging into local dishes."
                
                BAD:
                "Ensure you pack warm clothing suitable for temperatures dropping close
                to freezing."

                ==============================
                FINAL CHECK
                ==============================

                Before returning your response, make sure:

                - It is valid JSON.
                - There is no Markdown.
                - There is no text outside the JSON.
                - No information was invented.
                - Weather information comes from OpenWeather.
                - Holiday information comes from Nager.Date.
                - Destinations are ranked.
                - Scores are between 0 and 100.

                Return ONLY the JSON.

                """.formatted(
                userPreferences,
                externalData.toString()
        );
    }


    public String callGemini(String prompt) {

        String geminiUrl =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                        + "gemini-flash-lite-latest:generateContent?key="
                        + apiKey;


        JsonObject textPart = new JsonObject();

        textPart.addProperty("text", prompt);


        JsonArray parts = new JsonArray();

        parts.add(textPart);


        JsonObject content = new JsonObject();

        content.add("parts", parts);


        JsonArray contentsArray = new JsonArray();

        contentsArray.add(content);


        JsonObject requestBody = new JsonObject();

        requestBody.add("contents", contentsArray);


        Request request = new Request.Builder()
                .url(geminiUrl)
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();


        try (Response response =
                     client.newCall(request).execute()) {


            if (response.body() == null) {

                return "AI Error: Empty response body.";
            }


            String json = response.body().string();

            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();


            if (obj.has("error")) {

                return "AI Error: "
                        + obj.get("error")
                        .getAsJsonObject()
                        .get("message")
                        .getAsString();
            }


            return extractText(obj);


        } catch (Exception e) {

            return "AI Error: "
                    + e.getMessage();
        }
    }

    private String extractText(JsonObject obj) {

        JsonArray candidates =
                obj.getAsJsonArray("candidates");

        if (candidates == null || candidates.size() == 0) {
            throw new ApiException("Gemini returned no candidates");
        }


        JsonObject firstCandidate = candidates.get(0).getAsJsonObject();


        JsonObject content = firstCandidate.getAsJsonObject("content");


        JsonArray parts = content.getAsJsonArray("parts");


        return parts
                .get(0)
                .getAsJsonObject()
                .get("text")
                .getAsString();
    }


    public CountryComparisonDTO compareCountries(
            Integer travelRequestId,
            String country1,
            String country2
    ) {

        TravelRequest travelRequest =
                travelRequestRepository.findById(travelRequestId)
                        .orElseThrow(() ->
                                new ApiException("travel request not found"));

        StringBuilder preferences =
                new StringBuilder();

        if (travelRequest.getGeneralPreference() != null) {

            preferences.append("Weather: ")
                    .append(travelRequest
                            .getGeneralPreference()
                            .getWeather())
                    .append("\n");

            preferences.append("Environment: ")
                    .append(travelRequest
                            .getGeneralPreference()
                            .getEnvironment())
                    .append("\n");

            preferences.append("Crowd preference: ")
                    .append(travelRequest
                            .getGeneralPreference()
                            .getCrowdPreference())
                    .append("\n");

            preferences.append("Trip pace: ")
                    .append(travelRequest
                            .getGeneralPreference()
                            .getTripPace())
                    .append("\n");
        }

        if (travelRequest.getActivityPreferences() != null) {

            preferences.append("\nActivities:\n");

            travelRequest.getActivityPreferences()
                    .forEach(activity ->
                            preferences.append("- ")
                                    .append(activity.getActivityType())
                                    .append(" | priority: ")
                                    .append(activity.getPriority())
                                    .append("\n")
                    );
        }

        if (travelRequest.getTravelRestrictions() != null) {

            preferences.append("\nRestrictions:\n");

            travelRequest.getTravelRestrictions()
                    .forEach(restriction ->
                            preferences.append("- ")
                                    .append(restriction.getRestrictionType())
                                    .append(": ")
                                    .append(restriction.getDescription())
                                    .append(" | required: ")
                                    .append(restriction.getIsRequired())
                                    .append("\n")
                    );
        }

        String prompt = """
            You are the country comparison assistant
            for Sahl Darbak.

            Compare the following two countries for this traveler.

            COUNTRY 1:
            %s

            COUNTRY 2:
            %s

            TRAVELER PREFERENCES:
            %s

            Compare the countries based on:

            1. Weather
            2. Activities
            3. Environment
            4. Crowds
            5. Budget

            IMPORTANT:

            - Consider the traveler's preferences.
            - Consider their activities.
            - Consider their restrictions.
            - Do not invent specific prices.
            - Do not invent exact weather conditions.
            - Keep the recommendation practical.
            - Return ONLY valid JSON.
            - Do not use Markdown.

            Use EXACTLY this structure:

            {
              "country1": "%s",
              "country2": "%s",
              "betterForWeather": "Country name",
              "betterForActivities": "Country name",
              "betterForEnvironment": "Country name",
              "betterForCrowds": "Country name",
              "betterForBudget": "Country name",
              "recommendation": "Country name",
              "reason": "Short explanation"
            }
            """.formatted(
                country1,
                country2,
                preferences,
                country1,
                country2
        );

        try {

            String response = callGemini(prompt);

            return objectMapper.readValue(
                    response,
                    CountryComparisonDTO.class
            );

        } catch (Exception e) {

            throw new ApiException(
                    "failed to compare countries: "
                            + e.getMessage()
            );
        }
    }




}