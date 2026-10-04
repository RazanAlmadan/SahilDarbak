package com.example.sahldarbak.Service;


import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Model.TravelRestriction;
import com.example.sahldarbak.Repository.TravelRequestRepository;
import com.example.sahldarbak.Repository.TravelRestrictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelRestrictionService {


    private final TravelRestrictionRepository travelRestrictionRepository;
    private final TravelRequestRepository travelRequestRepository;


    // GET ALL
    public List<TravelRestriction> getTravelRestrictions() {
        return travelRestrictionRepository.findAll();
    }


    // ADD
    public void addTravelRestriction(Integer travelRequestId,
                                     TravelRestriction travelRestriction) {

        TravelRequest travelRequest = travelRequestRepository.findTravelRequestById(travelRequestId);

        // check if travel request exists
        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }

        // link travel restriction to travel request
        travelRestriction.setTravelRequest(travelRequest);

        travelRestrictionRepository.save(travelRestriction);
    }


    // UPDATE
    public void updateTravelRestriction(Integer travelRestrictionId, TravelRestriction travelRestriction) {

        TravelRestriction oldTravelRestriction = travelRestrictionRepository.findTravelRestrictionById(travelRestrictionId);

        // check if travel restriction exists
        if (oldTravelRestriction == null) {
            throw new ApiException("travel restriction not found");
        }

        oldTravelRestriction.setRestrictionType(travelRestriction.getRestrictionType());
        oldTravelRestriction.setDescription(travelRestriction.getDescription());
        oldTravelRestriction.setIsRequired(travelRestriction.getIsRequired());

        travelRestrictionRepository.save(oldTravelRestriction);
    }


    // DELETE
    public void deleteTravelRestriction(Integer travelRestrictionId) {

        TravelRestriction travelRestriction = travelRestrictionRepository.findTravelRestrictionById(travelRestrictionId);

        // check if travel restriction exists
        if (travelRestriction == null) {
            throw new ApiException("travel restriction not found");
        }

        travelRestrictionRepository.delete(travelRestriction);
    }
}
