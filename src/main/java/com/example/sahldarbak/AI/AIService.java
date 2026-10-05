package com.example.sahldarbak.AI;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.*;
import com.example.sahldarbak.Repository.TravelRequestRepository;
import com.example.sahldarbak.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIService {

    private final UserRepository userRepository;
    private final TravelRequestRepository travelRequestRepository;


    @Value("${gemini.api.key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();

    private final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-lite-latest:generateContent?key=" + apiKey;

    public void generateCountry(Integer travel_request_id){
        TravelRequest travelRequest = travelRequestRepository.findTravelRequestById(travel_request_id);
        if (travelRequest == null){
            throw new ApiException("travel request not found");
        }

        GeneralPreference g = travelRequest.getGeneralPreference();

        StringBuilder generalPreference = new StringBuilder();
        generalPreference.append("General Preference: ")
                .append("\n").append("Weather: ")
                .append(g.getWeather())
                .append("\n").append("Environment: ")
                .append(g.getEnvironment())
                .append("\n").append("Crowd Preference: ")
                .append(g.getCrowdPreference())
                .append("\n").append("Trip Pace: ")
                .append(g.getTripPace()).append("\n");


        int count = 1;
        StringBuilder foodPreference = new StringBuilder();
        for (FoodPreference f : travelRequest.getFoodPreferences()){
            foodPreference.append("Food Preference ")
                    .append(count)
                    .append(": ")
                    .append("\n")
                    .append("Food Type: ")
                    .append(f.getFoodType())
                    .append("isRequired: ")
                    .append(f.getIsRequired())
                    .append("\n");
            count ++;
        }

        StringBuilder activityPreference = new StringBuilder();
        for (ActivityPreference a : travelRequest.getActivityPreferences()){
            activityPreference.append("Activity Preference")
                    .append(count)
                    .append(": ")
                    .append("\n")
                    .append("Activity type: ")
                    .append(a.getActivityType())
                    .append("priority: ")
                    .append(a.getPriority())
                    .append("\n");
            count++;
        }

        StringBuilder restrictions = new StringBuilder();
        for (TravelRestriction r : travelRequest.getTravelRestrictions()){

        }


    }


}
