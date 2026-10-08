package com.example.sahldarbak.AI;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.ExternalApi.CountryService;
import com.example.sahldarbak.ExternalApi.HolidayService;
import com.example.sahldarbak.ExternalApi.WeatherService;
import com.example.sahldarbak.DTO.PackingList.PackingListDTO;
import com.example.sahldarbak.Model.*;
import com.example.sahldarbak.Repository.TripRepository;
import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PackingListAIService {

    private final WeatherService weatherService;
    private final HolidayService holidayService;

    private final TripRepository tripRepository;

    private final CountryService countryService;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();


    public PackingListDTO generatePackingList(Trip trip) {

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        TravelRequest travelRequest = trip.getTravelRequest();

        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }


        /*
         * STEP 1
         * Get weather for the destination.
         */

        JsonObject weather;

        try {

            weather = weatherService.getWeather(trip.getCity());

        } catch (Exception e) {

            throw new ApiException(
                    "Could not get weather information"
            );
        }


        /*
         * STEP 2
         * Get holidays during the trip.
         */

        JsonArray holidays;

        try {

            holidays = getHolidaysForTrip(
                    trip.getCountry(),
                    trip.getTravelRequest().getStartDate(),
                    trip.getTravelRequest().getEndDate()
            );

        } catch (Exception e) {

            throw new ApiException(
                    "Could not get holiday information"
            );
        }


        /*
         * STEP 3
         * Build the user's preferences.
         */

        String userPreferences =
                buildUserPreferences(travelRequest);


        /*
         * STEP 4
         * Give Gemini everything it needs.
         */

        String prompt = buildPrompt(
                trip,
                userPreferences,
                weather,
                holidays
        );


        /*
         * STEP 5
         * Ask Gemini for the packing list.
         */

        String aiResponse = callGemini(prompt);


        /*
         * STEP 6
         * Convert Gemini JSON into DTO.
         */

        try {

            Gson gson = new Gson();

            return gson.fromJson(
                    aiResponse,
                    PackingListDTO.class
            );

        } catch (Exception e) {

            throw new ApiException(
                    "AI returned invalid packing list JSON: "
                            + aiResponse
            );
        }
    }


    private String buildUserPreferences(
            TravelRequest travelRequest
    ) {

        StringBuilder text = new StringBuilder();


        /*
         * General preferences
         */

        GeneralPreference generalPreference =
                travelRequest.getGeneralPreference();

        if (generalPreference != null) {

            text.append("GENERAL PREFERENCES:\n");

            text.append("Weather Preference: ")
                    .append(generalPreference.getWeather())
                    .append("\n");

            text.append("Environment: ")
                    .append(generalPreference.getEnvironment())
                    .append("\n");

            text.append("Crowd Preference: ")
                    .append(generalPreference.getCrowdPreference())
                    .append("\n");

            text.append("Trip Pace: ")
                    .append(generalPreference.getTripPace())
                    .append("\n\n");
        }


        /*
         * Food preferences
         */

        text.append("FOOD PREFERENCES:\n");

        if (travelRequest.getFoodPreferences() != null) {

            for (FoodPreference food :
                    travelRequest.getFoodPreferences()) {

                text.append("- ")
                        .append(food.getFoodType())
                        .append(" | Required: ")
                        .append(food.getIsRequired())
                        .append("\n");
            }
        }

        text.append("\n");


        /*
         * Activity preferences
         */

        text.append("ACTIVITY PREFERENCES:\n");

        if (travelRequest.getActivityPreferences() != null) {

            for (ActivityPreference activity :
                    travelRequest.getActivityPreferences()) {

                text.append("- ")
                        .append(activity.getActivityType())
                        .append(" | Priority: ")
                        .append(activity.getPriority())
                        .append("\n");
            }
        }

        text.append("\n");


        /*
         * Travel restrictions
         */

        text.append("TRAVEL RESTRICTIONS:\n");

        if (travelRequest.getTravelRestrictions() != null) {

            for (TravelRestriction restriction :
                    travelRequest.getTravelRestrictions()) {

                text.append("- Type: ")
                        .append(restriction.getRestrictionType())
                        .append("\n");

                text.append("  Description: ")
                        .append(restriction.getDescription())
                        .append("\n");

                text.append("  Required: ")
                        .append(restriction.getIsRequired())
                        .append("\n");
            }
        }

        return text.toString();
    }


    private String buildPrompt(
            Trip trip,
            String userPreferences,
            JsonObject weather,
            JsonArray holidays
    ) {

        return """
                You are the AI travel packing assistant for Sahl Darbak.

                Your job is to create a personalized packing list.

                Be practical, friendly, fun and helpful.
                Do not sound like a formal travel agency.

                IMPORTANT RULES:

                1. Base the packing list on the actual destination.
                2. Consider the travel dates.
                3. Consider the weather data provided.
                4. Consider the user's activities.
                5. Consider the user's preferences.
                6. Consider all travel restrictions.
                7. If there are children, consider their needs.
                8. Do not invent weather information.
                9. Do not invent holidays.
                10. Do not include unnecessary items just to make the list longer.
                11. If a restriction requires special items, prioritize them.
                12. Return ONLY valid JSON.
                13. Do not use Markdown.
                14. Do not add explanations outside the JSON.

                
                Respond entirely in Arabic.
                Return the response in the required JSON format.
                Do not add any extra text outside the JSON.

                Use exactly this JSON structure:

                {
                  "status": "success",
                  "country": "Japan",
                  "city": "Tokyo",
                  "weatherSummary": "Cool weather with possible rain.",
                  "clothing": [],
                  "shoes": [],
                  "weatherEssentials": [],
                  "activityEssentials": [],
                  "travelEssentials": [],
                  "healthAndPersonal": [],
                  "tips": []
                }

                TRIP INFORMATION:

                Country:
                """ + trip.getCountry() + """

                City:
                """ + trip.getCity() + """

                Start Date:
                """ + trip.getTravelRequest().getStartDate() + """

                End Date:
                """ + trip.getTravelRequest().getEndDate() + """

                User Preferences:
                """ + userPreferences + """

                WEATHER DATA:
                """ + weather + """

                PUBLIC HOLIDAYS DURING THE TRIP:
                """ + holidays + """

                Create the personalized packing list now.
                """;
    }


    private String callGemini(String prompt) {

        String geminiUrl =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                        + "gemini-flash-lite-latest:generateContent?key="
                        + apiKey;


        JsonObject requestBody =
                new JsonObject();

        JsonArray contents =
                new JsonArray();

        JsonObject content =
                new JsonObject();

        JsonArray parts =
                new JsonArray();

        JsonObject part =
                new JsonObject();

        part.addProperty(
                "text",
                prompt
        );

        parts.add(part);

        content.add(
                "parts",
                parts
        );

        contents.add(content);

        requestBody.add(
                "contents",
                contents
        );


        Request request =
                new Request.Builder()
                        .url(geminiUrl)
                        .post(
                                RequestBody.create(
                                        requestBody.toString(),
                                        MediaType.parse(
                                                "application/json"
                                        )
                                )
                        )
                        .build();


        try (Response response =
                     client.newCall(request).execute()) {

            if (response.body() == null) {

                throw new ApiException(
                        "Gemini returned an empty response"
                );
            }

            String responseBody =
                    response.body().string();


            if (!response.isSuccessful()) {

                throw new ApiException(
                        "Gemini API error: "
                                + responseBody
                );
            }


            JsonObject json =
                    JsonParser.parseString(
                            responseBody
                    ).getAsJsonObject();


            String text =
                    json.getAsJsonArray("candidates")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0)
                            .getAsJsonObject()
                            .get("text")
                            .getAsString();


            /*
             * Gemini sometimes returns ```json ... ```.
             * Remove Markdown if it happens.
             */

            text = text
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();


            return text;

        } catch (Exception e) {

            throw new ApiException(
                    "Failed to generate packing list: "
                            + e.getMessage()
            );
        }
    }


    private JsonArray getHolidaysForTrip(
            String country,
            LocalDate startDate,
            LocalDate endDate
    ) {

        String countryCode =
                countryService.getCountryCode(country);

        JsonArray result =
                new JsonArray();


        JsonArray startYearHolidays =
                holidayService.getHolidays(
                        countryCode,
                        startDate.getYear()
                );


        addRelevantHolidays(
                result,
                startYearHolidays,
                startDate,
                endDate
        );


        if (startDate.getYear() != endDate.getYear()) {

            JsonArray endYearHolidays =
                    holidayService.getHolidays(
                            countryCode,
                            endDate.getYear()
                    );

            addRelevantHolidays(
                    result,
                    endYearHolidays,
                    startDate,
                    endDate
            );
        }


        return result;
    }

    private void addRelevantHolidays(
            JsonArray result,
            JsonArray holidays,
            LocalDate startDate,
            LocalDate endDate
    ) {

        for (int i = 0; i < holidays.size(); i++) {

            JsonObject holiday =
                    holidays.get(i)
                            .getAsJsonObject();

            if (!holiday.has("date")) {
                continue;
            }

            LocalDate holidayDate =
                    LocalDate.parse(
                            holiday.get("date")
                                    .getAsString()
                    );

            if (!holidayDate.isBefore(startDate)
                    && !holidayDate.isAfter(endDate)) {

                result.add(holiday);
            }
        }
    }
}
