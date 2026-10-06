package com.example.sahldarbak.Controller;


import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Service.TravelRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/travel-request")
@RequiredArgsConstructor
public class TravelRequestController {

    private final TravelRequestService travelRequestService;

    // GET
    @GetMapping("/get")
    public ResponseEntity<?>getTravelRequests(){
        return ResponseEntity.status(200).body(travelRequestService.getTravelRequests());
    }


    // ADD
    @PostMapping("/add/{userId}")
    public ResponseEntity<?> addTravelRequest(@PathVariable Integer userId,@Valid @RequestBody TravelRequest travelRequest) {
        travelRequestService.addTravelRequest(userId, travelRequest);
        return ResponseEntity.status(200).body(new ApiResponse("travel request added successfully"));
    }


    // UPDATE
    @PutMapping("/update/{travelRequestId}")
    public ResponseEntity<?> updateTravelRequest(@PathVariable Integer travelRequestId, @Valid @RequestBody TravelRequest travelRequest) {
        travelRequestService.updateTravelRequest(travelRequestId, travelRequest);
        return ResponseEntity.status(200).body(new ApiResponse("travel request updated successfully"));
    }


    // DELETE
    @DeleteMapping("/delete/{travelRequestId}")
    public ResponseEntity<?> deleteTravelRequest(@PathVariable Integer travelRequestId) {
        travelRequestService.deleteTravelRequest(travelRequestId);
        return ResponseEntity.status(200).body(new ApiResponse("travel request deleted successfully"));
    }


    // SUBMIT
    @PutMapping("/submit-travel-request/{travelRequestId}")
    public ResponseEntity<?> submitTravelRequest(@PathVariable Integer travelRequestId) {
        travelRequestService.submitTravelRequest(travelRequestId);
        return ResponseEntity.status(200).body(new ApiResponse("travel request submitted successfully"));
    }

    @GetMapping("/get-by-user/{userId}")
    public ResponseEntity<?> getTravelRequestsByUser(@PathVariable Integer userId) {

        return ResponseEntity.status(200).body(travelRequestService.getTravelRequestsByUser(userId));
    }
}
