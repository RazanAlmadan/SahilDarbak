package com.example.sahldarbak.Controller;

import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.Model.TripPlace;
import com.example.sahldarbak.Service.TripPlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trip-place")
@RequiredArgsConstructor
public class TripPlaceController {

    private final TripPlaceService tripPlaceService;

    // get all
    @GetMapping("/get")
    public ResponseEntity<List<TripPlace>> getAllTripPlaces() {
        return ResponseEntity.status(200).body(tripPlaceService.getAllTripPlaces());
    }

    // add
    @PostMapping("/add/{itineraryId}")
    public ResponseEntity<ApiResponse> addTripPlace(@PathVariable Integer itineraryId, @Valid @RequestBody TripPlace tripPlace) {

        tripPlaceService.addTripPlace(itineraryId, tripPlace);

        return ResponseEntity.status(200).body(new ApiResponse("Trip place added successfully"));
    }

    // update
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateTripPlace(@PathVariable Integer id, @Valid @RequestBody TripPlace tripPlace) {
        tripPlaceService.updateTripPlace(id, tripPlace);
        return ResponseEntity.status(200).body(new ApiResponse("Trip place updated successfully"));
    }

    // delete
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteTripPlace(@PathVariable Integer id) {
        tripPlaceService.deleteTripPlace(id);
        return ResponseEntity.status(200).body(new ApiResponse("Trip place deleted successfully"));
    }

    //EXTRA ENDPOINT
    @GetMapping("/get-by-date/{tripId}/{date}")
    public ResponseEntity<?> getTripPlacesByDate(@PathVariable Integer tripId, @PathVariable LocalDate date) {
        return ResponseEntity.status(200).body(tripPlaceService.getTripPlacesByDate(tripId, date));
    }

    // GET TODAY'S PLAN
    @GetMapping("/today/{tripId}")
    public ResponseEntity<?> getTodayPlan(@PathVariable Integer tripId) {
        return ResponseEntity.status(200).body(tripPlaceService.getTodayPlan(tripId));
    }

    @GetMapping("/get-by-trip/{tripId}")
    public ResponseEntity<?> getTripPlacesByTrip(@PathVariable Integer tripId) {
        return ResponseEntity.status(200).body(tripPlaceService.getTripPlacesByTrip(tripId));
    }
}
