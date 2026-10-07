package com.example.sahldarbak.ExternalApi;


import com.example.sahldarbak.Api.ApiException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
@RequiredArgsConstructor
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

            if (!response.isSuccessful() || response.body() == null) {
                throw new ApiException(
                        "failed to get weather information"
                );
            }

            String responseBody = response.body().string();

            if (responseBody.isEmpty()) {
                throw new ApiException(
                        "weather response is empty"
                );
            }

            return JsonParser
                    .parseString(responseBody)
                    .getAsJsonObject();

        } catch (IOException e) {

            throw new ApiException(
                    "failed to connect to weather service"
            );
        }
    }
}