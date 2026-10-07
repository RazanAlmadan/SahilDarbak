package com.example.sahldarbak.ExternalApi;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

@Service
public class HolidayService {

    private final OkHttpClient client = new OkHttpClient();

    /// Nager.date API
    private final String API_URL =
            "https://date.nager.at/api/v3/PublicHolidays";


    /// returns the Holidays so Gemini Can use it.
    public JsonArray getHolidays(String countryCode, int year) {

        String url = API_URL + "/" + year + "/" + countryCode;

        System.out.println("NAGER REQUEST: " + url);

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {

            if (response.body() == null) {
                throw new RuntimeException("Nager.Date API returned an empty response");
            }

            String responseBody = response.body().string();

            System.out.println("NAGER STATUS: " + response.code());

            if (!response.isSuccessful()) {
                throw new RuntimeException("Nager.Date API error: " + responseBody);
            }

            return JsonParser.parseString(responseBody).getAsJsonArray();

        } catch (Exception e) {

            throw new RuntimeException("Failed to get holidays for " + countryCode + ": " + e.getMessage());
        }
    }
}