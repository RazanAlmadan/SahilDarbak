package com.example.sahldarbak.Controller;


import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.Model.ActivityPreference;
import com.example.sahldarbak.Service.ActivityPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activity-preference")
@RequiredArgsConstructor
public class ActivityPreferenceController {


    private final ActivityPreferenceService activityPreferenceService;


    // GET ALL
    @GetMapping("/get-activity-preferences")
    public ResponseEntity<?> getActivityPreferences() {
        return ResponseEntity.status(200).body(activityPreferenceService.getActivityPreferences());
    }


    // ADD
    @PostMapping("/add-activity-preference/{travelRequestId}")
    public ResponseEntity<?> addActivityPreference(@PathVariable Integer travelRequestId, @Valid @RequestBody ActivityPreference activityPreference) {
        activityPreferenceService.addActivityPreference(travelRequestId, activityPreference);
        return ResponseEntity.status(200).body(new ApiResponse("activity preference added successfully"));
    }


    // UPDATE
    @PutMapping("/update-activity-preference/{activityPreferenceId}")
    public ResponseEntity<?> updateActivityPreference(@PathVariable Integer activityPreferenceId, @Valid @RequestBody ActivityPreference activityPreference) {
        activityPreferenceService.updateActivityPreference(activityPreferenceId, activityPreference);
        return ResponseEntity.status(200).body(new ApiResponse("activity preference updated successfully"));
    }


    // DELETE
    @DeleteMapping("/delete-activity-preference/{activityPreferenceId}")
    public ResponseEntity<?> deleteActivityPreference(@PathVariable Integer activityPreferenceId) {
        activityPreferenceService.deleteActivityPreference(activityPreferenceId);
        return ResponseEntity.status(200).body(new ApiResponse("activity preference deleted successfully"));
    }
}
