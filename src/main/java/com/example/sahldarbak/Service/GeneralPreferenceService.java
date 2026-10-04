package com.example.sahldarbak.Service;


import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.GeneralPreference;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Repository.GeneralPreferenceRepository;
import com.example.sahldarbak.Repository.TravelRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeneralPreferenceService {
    private final GeneralPreferenceRepository generalPreferenceRepository;
    private final TravelRequestRepository travelRequestRepository;


    // GET ALL
    public List<GeneralPreference> getGeneralPreferences() {
        return generalPreferenceRepository.findAll();
    }


    // ADD
    public void addGeneralPreference(Integer travelRequestId, GeneralPreference generalPreference) {

        TravelRequest travelRequest = travelRequestRepository.findTravelRequestById(travelRequestId);

        // check if travel request exists
        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }

        // each travel request can have only one general preference
        GeneralPreference checkPreference = generalPreferenceRepository.findGeneralPreferenceByTravelRequest_Id(travelRequestId);

        if (checkPreference != null) {
            throw new ApiException("general preference already exists for this travel request");
        }

        // link general preference to travel request
        generalPreference.setTravelRequest(travelRequest);

        generalPreferenceRepository.save(generalPreference);
    }


    // UPDATE
    public void updateGeneralPreference(Integer generalPreferenceId, GeneralPreference generalPreference) {

        GeneralPreference oldGeneralPreference = generalPreferenceRepository.findGeneralPreferenceById(generalPreferenceId);

        // check if general preference exists
        if (oldGeneralPreference == null) {
            throw new ApiException("general preference not found");
        }

        oldGeneralPreference.setWeather(generalPreference.getWeather());
        oldGeneralPreference.setEnvironment(generalPreference.getEnvironment());
        oldGeneralPreference.setCrowdPreference(generalPreference.getCrowdPreference());
        oldGeneralPreference.setTripPace(generalPreference.getTripPace());

        generalPreferenceRepository.save(oldGeneralPreference);
    }


    // DELETE
    public void deleteGeneralPreference(Integer generalPreferenceId) {

        GeneralPreference generalPreference = generalPreferenceRepository.findGeneralPreferenceById(generalPreferenceId);

        // check if general preference exists
        if (generalPreference == null) {
            throw new ApiException("general preference not found");
        }

        generalPreferenceRepository.delete(generalPreference);
    }



}
