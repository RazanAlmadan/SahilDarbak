package com.example.sahldarbak.Service;


import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.FoodPreference;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Repository.FoodPreferenceRepository;
import com.example.sahldarbak.Repository.TravelRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodPreferenceService {

    private final FoodPreferenceRepository foodPreferenceRepository;
    private final TravelRequestRepository travelRequestRepository;


    // GET ALL
    public List<FoodPreference> getFoodPreferences() {
        return foodPreferenceRepository.findAll();
    }


    // ADD
    public void addFoodPreference(Integer travelRequestId, FoodPreference foodPreference) {

        TravelRequest travelRequest = travelRequestRepository.findTravelRequestById(travelRequestId);

        // check if travel request exists
        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }

        // link food preference to travel request
        foodPreference.setTravelRequest(travelRequest);

        foodPreferenceRepository.save(foodPreference);
    }


    // UPDATE
    public void updateFoodPreference(Integer foodPreferenceId, FoodPreference foodPreference) {

        FoodPreference oldFoodPreference = foodPreferenceRepository.findFoodPreferenceById(foodPreferenceId);

        // check if food preference exists
        if (oldFoodPreference == null) {
            throw new ApiException("food preference not found");
        }

        oldFoodPreference.setFoodType(foodPreference.getFoodType());
        oldFoodPreference.setIsRequired(foodPreference.getIsRequired());

        foodPreferenceRepository.save(oldFoodPreference);
    }


    // DELETE
    public void deleteFoodPreference(Integer foodPreferenceId) {

        FoodPreference foodPreference = foodPreferenceRepository.findFoodPreferenceById(foodPreferenceId);

        // check if food preference exists
        if (foodPreference == null) {
            throw new ApiException("food preference not found");
        }

        foodPreferenceRepository.delete(foodPreference);
    }
}
