package com.example.sahldarbak.Controller;


import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.Model.TravelRestriction;
import com.example.sahldarbak.Service.TravelRestrictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/travel-restriction")
@RequiredArgsConstructor
public class TravelRestrictionController {


    private final TravelRestrictionService travelRestrictionService;


    // GET ALL
    @GetMapping("/get-travel-restrictions")
    public ResponseEntity<?> getTravelRestrictions() {
        return ResponseEntity.status(200).body(travelRestrictionService.getTravelRestrictions());
    }


    // ADD
    @PostMapping("/add-travel-restriction/{travelRequestId}")
    public ResponseEntity<?> addTravelRestriction(@PathVariable Integer travelRequestId, @Valid @RequestBody TravelRestriction travelRestriction) {
        travelRestrictionService.addTravelRestriction(travelRequestId, travelRestriction);
        return ResponseEntity.status(200).body(new ApiResponse("travel restriction added successfully"));
    }


    // UPDATE
    @PutMapping("/update-travel-restriction/{travelRestrictionId}")
    public ResponseEntity<?> updateTravelRestriction(@PathVariable Integer travelRestrictionId, @Valid @RequestBody TravelRestriction travelRestriction) {
        travelRestrictionService.updateTravelRestriction(travelRestrictionId, travelRestriction);
        return ResponseEntity.status(200).body(new ApiResponse("travel restriction updated successfully"));
    }


    // DELETE
    @DeleteMapping("/delete-travel-restriction/{travelRestrictionId}")
    public ResponseEntity<?> deleteTravelRestriction(@PathVariable Integer travelRestrictionId) {
        travelRestrictionService.deleteTravelRestriction(travelRestrictionId);
        return ResponseEntity.status(200).body(new ApiResponse("travel restriction deleted successfully"));
    }
}
