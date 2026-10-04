package com.example.sahldarbak.Service;


import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Repository.TravelRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelRequestService {

    private final TravelRequestRepository travelRequestRepository;


    //CRUD METHOD
    //GET ALL (read)
    public List<TravelRequest>getTravelRequests(){
        return travelRequestRepository.findAll();
    }

    //ADD(create)
    public void addTravelRequest(TravelRequest travelRequest){

        // end date must be after start date
        if (!travelRequest.getEndDate().isAfter(travelRequest.getStartDate())) {
            throw new ApiException("end date must be after start date");
        }

        if (travelRequest.getTravelType().equals("solo")) {
            // solo travel does not require group or family details
            travelRequest.setGroupSize(null);
            travelRequest.setAdultsCount(null);

        } else if (travelRequest.getTravelType().equals("group")) {

            // group size is required when travel type is group
            if (travelRequest.getGroupSize() == null) {
                throw new ApiException("group size is required for group travel");
            }

            // group travel does not require adults count
            travelRequest.setAdultsCount(null);

        } else if (travelRequest.getTravelType().equals("family")) {

            if (travelRequest.getAdultsCount() == null) {
                throw new ApiException("adults count is required for family travel");
            }

            // family travel does not require group size
            travelRequest.setGroupSize(null);
        }

        // status is set automatically when creating a new travel request
        travelRequest.setStatus("open");
        travelRequestRepository.save(travelRequest);
    }


    //UPDATE
    public void updateTravelRequest(Integer travel_request_id, TravelRequest travelRequest){

        TravelRequest oldTravelRequest = travelRequestRepository.findTravelRequestById(travel_request_id);

        // check if travel request exists
        if (oldTravelRequest == null) {
            throw new ApiException("travel request not found");
        }

        // end date must be after start date
        if (!travelRequest.getEndDate().isAfter(travelRequest.getStartDate())) {
            throw new ApiException("end date must be after start date");
        }

        // check required data based on travel type
        if (travelRequest.getTravelType().equals("solo")) {

            travelRequest.setGroupSize(null);
            travelRequest.setAdultsCount(null);

        } else if (travelRequest.getTravelType().equals("group")) {

            // group size is required when travel type is group
            if (travelRequest.getGroupSize() == null) {
                throw new ApiException("group size is required for group travel");
            }

            travelRequest.setAdultsCount(null);

        } else if (travelRequest.getTravelType().equals("family")) {

            // adults count is required when travel type is family
            if (travelRequest.getAdultsCount() == null) {
                throw new ApiException("adults count is required for family travel");
            }

            travelRequest.setGroupSize(null);
        }

        oldTravelRequest.setStartDate(travelRequest.getStartDate());
        oldTravelRequest.setEndDate(travelRequest.getEndDate());
        oldTravelRequest.setBudget(travelRequest.getBudget());
        oldTravelRequest.setTravelType(travelRequest.getTravelType());
        oldTravelRequest.setGroupSize(travelRequest.getGroupSize());
        oldTravelRequest.setAdultsCount(travelRequest.getAdultsCount());

        // status is not changed through normal update
        travelRequestRepository.save(oldTravelRequest);
    }


    //DELETE
    public void deleteTravelRequest(Integer travel_request_id){

        TravelRequest travelRequest = travelRequestRepository.findTravelRequestById(travel_request_id);

        // check if travel request exists
        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }

        travelRequestRepository.delete(travelRequest);
    }
}
