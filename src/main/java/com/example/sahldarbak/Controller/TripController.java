package com.example.sahldarbak.Controller;


import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.DTO.CityPlan.CityPlanDTO;
import com.example.sahldarbak.DTO.DestinationRecommendation.SelectDestinationDTO;
import com.example.sahldarbak.DTO.PackingList.PackingListDTO;
import com.example.sahldarbak.DTO.SmartItinerary.SmartItineraryDTO;
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

    //EXTRA END POINTS:

    @PostMapping("/generate-smart-itinerary/{tripId}")
    public ResponseEntity<?> generateSmartItinerary(@PathVariable Integer tripId) {
        SmartItineraryDTO smartItineraryDTO=tripService.generateSmartItinerary(tripId);
        return ResponseEntity.status(200).body(smartItineraryDTO);
    }

    // user enter the country they want in the body
    @PostMapping("/select/{user_id}/{travel_request_id}")
    public ResponseEntity<ApiResponse> selectDestination(@PathVariable Integer user_id, @PathVariable Integer travel_request_id, @Valid @RequestBody SelectDestinationDTO dto) {

        tripService.selectDestination(user_id, travel_request_id, dto);

        return ResponseEntity.status(200).body(new ApiResponse("Destination selected successfully"));
    }

    // GENERATE AI CITY PLAN
    @PostMapping("/generate-city-plan/{tripId}")
    public ResponseEntity<?> generateCityPlan(@PathVariable Integer tripId) {
        return ResponseEntity.status(200).body(tripService.generateCityPlan(tripId));
    }

    @GetMapping("/get-by-travel-request/{travelRequestId}")
    public ResponseEntity<?> getTripByTravelRequest(@PathVariable Integer travelRequestId) {
        return ResponseEntity.status(200).body(tripService.getTripByTravelRequest(travelRequestId));
    }


    @PostMapping("/generate-packing-list/{tripId}")
    public ResponseEntity<?> generatePackingList(
            @PathVariable Integer tripId
    ) {

        PackingListDTO packingListDTO = tripService.generatePackingList(tripId);

        return ResponseEntity.status(200).body(packingListDTO);
    }


}