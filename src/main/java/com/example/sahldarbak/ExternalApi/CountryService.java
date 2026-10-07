package com.example.sahldarbak.ExternalApi;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

@Service
public class CountryService {

    private final OkHttpClient client = new OkHttpClient();

    private final String API_URL =
            "https://date.nager.at/api/v3/AvailableCountries";


    public String getCountryCode(String countryName) {

        Request request = new Request.Builder()
                .url(API_URL)
                .get()
                .build();

        try (Response response =
                     client.newCall(request).execute()) {

            if (response.body() == null) {
                throw new RuntimeException(
                        "Country API returned an empty response"
                );
            }

            String responseBody =
                    response.body().string();

            if (!response.isSuccessful()) {
                throw new RuntimeException(
                        "Country API error: "
                                + responseBody
                );
            }

            JsonArray countries =
                    JsonParser
                            .parseString(responseBody)
                            .getAsJsonArray();


            for (int i = 0; i < countries.size(); i++) {

                JsonObject country =
                        countries.get(i)
                                .getAsJsonObject();

                String name =
                        country.get("name")
                                .getAsString();

                if (name.equalsIgnoreCase(countryName)) {

                    return country
                            .get("countryCode")
                            .getAsString();
                }
            }


            throw new RuntimeException(
                    "Country code not found for "
                            + countryName
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to find country code for "
                            + countryName
                            + ": "
                            + e.getMessage()
            );
        }
    }
}