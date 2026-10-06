package com.example.sahldarbak.ExternalApi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    @Value("${openweather.api.key}")
    private String apiKey;

    @Value("${openweather.api.url}")
    private String apiUrl;

    private final OkHttpClient client = new OkHttpClient();


    public JsonObject getWeather(String city) {

        String url = apiUrl
                + "?q=" + city
                + "&appid=" + apiKey
                + "&units=metric";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (response.body() == null) {
                throw new RuntimeException("Weather API returned an empty response");
            }

            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                throw new RuntimeException(
                        "Weather API error: " + responseBody
                );
            }

            return JsonParser.parseString(responseBody).getAsJsonObject();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to get weather for " + city + ": " + e.getMessage()
            );
        }
    }
}