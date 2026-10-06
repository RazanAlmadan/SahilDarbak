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

    private final String API_URL =
            "https://nagerholidays.com/api/v4/Holidays/AT/2026";


    public JsonArray getHolidays(String countryCode, int year) {

        String url = API_URL
                + "/" + year
                + "/" + countryCode;

        System.out.println("NAGER REQUEST: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (response.body() == null) {
                throw new RuntimeException(
                        "Nager.Date API returned an empty response"
                );
            }

            String responseBody = response.body().string();

            System.out.println("NAGER STATUS: " + response.code());
            System.out.println("NAGER RESPONSE: " + responseBody);

            if (!response.isSuccessful()) {
                throw new RuntimeException(
                        "Nager.Date API error: " + responseBody
                );
            }

            return JsonParser
                    .parseString(responseBody)
                    .getAsJsonArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to get holidays for "
                            + countryCode
                            + ": "
                            + e.getMessage()
            );
        }
    }




}

