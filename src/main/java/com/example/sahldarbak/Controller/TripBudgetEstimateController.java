package com.example.sahldarbak.Controller;

import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.DTO.BudgetEstimateRequestDTO;
import com.example.sahldarbak.Model.TripBudgetEstimate;
import com.example.sahldarbak.Service.TripBudgetEstimateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trip-budget-estimate")
@RequiredArgsConstructor
public class TripBudgetEstimateController {

    private final TripBudgetEstimateService tripBudgetEstimateService;

    // get
    @GetMapping("/get")
    public ResponseEntity<List<TripBudgetEstimate>> getAllTripBudgetEstimates() {
        return ResponseEntity.status(200).body(tripBudgetEstimateService.getAllTripBudgetEstimates());
    }

    // add
    @PostMapping("/add/{trip_id}")
    public ResponseEntity<ApiResponse> addTripBudgetEstimate(@PathVariable Integer trip_id, @Valid @RequestBody TripBudgetEstimate tripBudgetEstimate) {
        tripBudgetEstimateService.addTripBudgetEstimate(trip_id, tripBudgetEstimate);
        return ResponseEntity.status(200).body(new ApiResponse("Trip budget estimate added successfully"));
    }

    // update
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateTripBudgetEstimate(@PathVariable Integer id, @Valid @RequestBody TripBudgetEstimate tripBudgetEstimate) {
        tripBudgetEstimateService.updateTripBudgetEstimate(id, tripBudgetEstimate);
        return ResponseEntity.status(200).body(new ApiResponse("Trip budget estimate updated successfully"));
    }

    // delete
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteTripBudgetEstimate(@PathVariable Integer id) {
        tripBudgetEstimateService.deleteTripBudgetEstimate(id);
        return ResponseEntity.status(200).body(new ApiResponse("Trip budget estimate deleted successfully"));
    }


    //EXTRA ENDPOINT

    // GENERATE AI BUDGET ESTIMATE
    @PostMapping("/generate/{tripId}")
    public ResponseEntity<?> generateBudgetEstimate(@PathVariable Integer tripId, @RequestBody @Valid BudgetEstimateRequestDTO requestDTO, @RequestParam(defaultValue = "ar") String lang) {

        return ResponseEntity.status(200).body(tripBudgetEstimateService.generateBudgetEstimate(tripId, requestDTO,lang));
    }


    // GET BUDGET ESTIMATE BY TRIP
    @GetMapping("/get-by-trip/{tripId}")
    public ResponseEntity<?> getBudgetEstimateByTrip(@PathVariable Integer tripId) {

        return ResponseEntity.status(200).body(tripBudgetEstimateService.getBudgetEstimateByTrip(tripId));
    }


    // REFRESH AI BUDGET ESTIMATE
    @PutMapping("/refresh/{tripId}")
    public ResponseEntity<?> refreshBudgetEstimate(@PathVariable Integer tripId, @RequestBody @Valid BudgetEstimateRequestDTO requestDTO, @RequestParam(defaultValue = "ar") String lang) {

        return ResponseEntity.status(200).body(tripBudgetEstimateService.refreshBudgetEstimate(tripId, requestDTO,lang));
    }
}
