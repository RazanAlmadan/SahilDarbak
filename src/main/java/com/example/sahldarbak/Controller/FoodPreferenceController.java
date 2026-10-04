package com.example.sahldarbak.Controller;


import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.Model.FoodPreference;
import com.example.sahldarbak.Service.FoodPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/food-preference")
@RequiredArgsConstructor
public class FoodPreferenceController {

    private final FoodPreferenceService foodPreferenceService;


    // GET ALL
    @GetMapping("/get-food-preferences")
    public ResponseEntity<?> getFoodPreferences() {
        return ResponseEntity.status(200).body(foodPreferenceService.getFoodPreferences());
    }


    // ADD
    @PostMapping("/add-food-preference/{travelRequestId}")
    public ResponseEntity<?> addFoodPreference(@PathVariable Integer travelRequestId, @Valid @RequestBody FoodPreference foodPreference) {
        foodPreferenceService.addFoodPreference(travelRequestId, foodPreference);
        return ResponseEntity.status(200).body(new ApiResponse("food preference added successfully"));
    }


    // UPDATE
    @PutMapping("/update-food-preference/{foodPreferenceId}")
    public ResponseEntity<?> updateFoodPreference(@PathVariable Integer foodPreferenceId, @Valid @RequestBody FoodPreference foodPreference) {
        foodPreferenceService.updateFoodPreference(foodPreferenceId, foodPreference);

        return ResponseEntity.status(200).body(new ApiResponse("food preference updated successfully"));
    }


    // DELETE
    @DeleteMapping("/delete-food-preference/{foodPreferenceId}")
    public ResponseEntity<?> deleteFoodPreference(@PathVariable Integer foodPreferenceId) {
        foodPreferenceService.deleteFoodPreference(foodPreferenceId);
        return ResponseEntity.status(200).body(new ApiResponse("food preference deleted successfully"));
    }
}
