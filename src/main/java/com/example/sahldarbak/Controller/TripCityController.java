package com.example.sahldarbak.Controller;


import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.Model.TripCity;
import com.example.sahldarbak.Service.TripCityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/trip-city")
@RequiredArgsConstructor
public class TripCityController {

    private final TripCityService tripCityService;


    // GET ALL
    @GetMapping("/get")
    public ResponseEntity<?> getTripCities() {
        return ResponseEntity.status(200).body(tripCityService.getTripCities());
    }


    // GET CITIES FOR SPECIFIC TRIP
    @GetMapping("/get-by-trip/{tripId}")
    public ResponseEntity<?> getTripCitiesByTripId(@PathVariable Integer tripId) {
        return ResponseEntity.status(200).body(tripCityService.getTripCitiesByTripId(tripId));
    }


    // ADD
    @PostMapping("/add/{tripId}")
    public ResponseEntity<?> addTripCity(@PathVariable Integer tripId, @RequestBody @Valid TripCity tripCity) {
        tripCityService.addTripCity(tripId, tripCity);
        return ResponseEntity.status(200).body(new ApiResponse("trip city added successfully"));
    }


    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTripCity(@PathVariable Integer id, @RequestBody @Valid TripCity tripCity) {
        tripCityService.updateTripCity(id, tripCity);
        return ResponseEntity.status(200).body(new ApiResponse("trip city updated successfully"));
    }


    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTripCity(@PathVariable Integer id) {
        tripCityService.deleteTripCity(id);
        return ResponseEntity.status(200).body(new ApiResponse("trip city deleted successfully"));
    }

    //EXTRA ENDPOINT

    @PostMapping("/accept-city-plan/{tripId}")
    public ResponseEntity<?> acceptCityPlan(@PathVariable Integer tripId) {
        tripCityService.acceptCityPlan(tripId);
        return ResponseEntity.status(200).body(new ApiResponse("city plan accepted successfully"));
    }

    @DeleteMapping("/cancel-city-plan/{tripId}")
    public ResponseEntity<?> cancelCityPlan(@PathVariable Integer tripId) {
        tripCityService.cancelCityPlan(tripId);
        return ResponseEntity.status(200).body(new ApiResponse("suggested city plan cancelled successfully"));
    }
}
