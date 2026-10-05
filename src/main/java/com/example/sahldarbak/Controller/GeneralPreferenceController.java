package com.example.sahldarbak.Controller;


import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.Model.GeneralPreference;
import com.example.sahldarbak.Service.GeneralPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/general-preference")
@RequiredArgsConstructor
public class GeneralPreferenceController {


    private final GeneralPreferenceService generalPreferenceService;


    // GET ALL
    @GetMapping("/get")
    public ResponseEntity<?> getGeneralPreferences() {
        return ResponseEntity.status(200).body(generalPreferenceService.getGeneralPreferences());
    }


    // ADD
    @PostMapping("/add/{travelRequestId}")
    public ResponseEntity<?> addGeneralPreference(@PathVariable Integer travelRequestId, @Valid @RequestBody GeneralPreference generalPreference) {
        generalPreferenceService.addGeneralPreference(travelRequestId, generalPreference);
        return ResponseEntity.status(200).body(new ApiResponse("general preference added successfully"));
    }


    // UPDATE
    @PutMapping("/update/{generalPreferenceId}")
    public ResponseEntity<?> updateGeneralPreference(@PathVariable Integer generalPreferenceId, @Valid @RequestBody GeneralPreference generalPreference) {
        generalPreferenceService.updateGeneralPreference(generalPreferenceId, generalPreference);
        return ResponseEntity.status(200).body(new ApiResponse("general preference updated successfully"));
    }


    // DELETE
    @DeleteMapping("/delete/{generalPreferenceId}")
    public ResponseEntity<?> deleteGeneralPreference(@PathVariable Integer generalPreferenceId) {
        generalPreferenceService.deleteGeneralPreference(generalPreferenceId);
        return ResponseEntity.status(200).body(new ApiResponse("general preference deleted successfully"));
    }
}
