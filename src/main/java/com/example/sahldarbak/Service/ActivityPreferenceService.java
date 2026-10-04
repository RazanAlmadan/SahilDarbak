package com.example.sahldarbak.Service;


import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.ActivityPreference;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Repository.TravelRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityPreferenceService {
    private final ActivityPreferenceRepository activityPreferenceRepository;
    private final TravelRequestRepository travelRequestRepository;



    // GET ALL
    public List<ActivityPreference> getActivityPreferences() {
        return activityPreferenceRepository.findAll();
    }


    // ADD
    public void addActivityPreference(Integer travelRequestId, ActivityPreference activityPreference) {

        TravelRequest travelRequest = travelRequestRepository.findTravelRequestById(travelRequestId);

        // check if travel request exists
        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }

        // link activity preference to travel request
        activityPreference.setTravelRequest(travelRequest);
        activityPreferenceRepository.save(activityPreference);
    }


    // UPDATE
    public void updateActivityPreference(Integer activityPreferenceId, ActivityPreference activityPreference) {

        ActivityPreference oldActivityPreference = activityPreferenceRepository.findActivityPreferenceById(activityPreferenceId);

        // check if activity preference exists
        if (oldActivityPreference == null) {
            throw new ApiException("activity preference not found");
        }

        oldActivityPreference.setActivityType(activityPreference.getActivityType());
        oldActivityPreference.setPriority(activityPreference.getPriority());

        activityPreferenceRepository.save(oldActivityPreference);
    }


    // DELETE
    public void deleteActivityPreference(Integer activityPreferenceId) {

        ActivityPreference activityPreference = activityPreferenceRepository.findActivityPreferenceById(activityPreferenceId);

        // check if activity preference exists
        if (activityPreference == null) {
            throw new ApiException("activity preference not found");
        }

        activityPreferenceRepository.delete(activityPreference);
    }
}
