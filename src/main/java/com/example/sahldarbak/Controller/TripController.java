package com.example.sahldarbak.Controller;


import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.Model.Trip;
import com.example.sahldarbak.Service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trip")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    // get all
    @GetMapping("/get")
    public ResponseEntity<List<Trip>> getAllTrips() {
        return ResponseEntity.status(200).body(tripService.getAllTrips());
    }

    // add
    @PostMapping("/add/{user_id}/{travel_request_id}")
    public ResponseEntity<ApiResponse> addTrip(@PathVariable Integer user_id, @PathVariable Integer travel_request_id, @Valid @RequestBody Trip trip) {
        tripService.addTrip(user_id, travel_request_id, trip);
        return ResponseEntity.status(200).body(new ApiResponse("Trip added successfully"));
    }

    // update
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateTrip(@PathVariable Integer id, @Valid @RequestBody Trip trip) {
        tripService.updateTrip(id, trip);
        return ResponseEntity.status(200).body(new ApiResponse("Trip updated successfully"));
    }

    // delete
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteTrip(@PathVariable Integer id) {
        tripService.deleteTrip(id);
        return ResponseEntity.status(200).body(new ApiResponse("Trip deleted successfully"));
    }
}
