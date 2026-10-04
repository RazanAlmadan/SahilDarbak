package com.example.sahldarbak.Service;


import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Repository.ChildRepository;
import com.example.sahldarbak.Repository.TravelRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelRequestService {

    private final TravelRequestRepository travelRequestRepository;
    private final ChildRepository childRepository;


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

        // request is incomplete until the user submits all preferences
        travelRequest.setStatus("draft");
        travelRequestRepository.save(travelRequest);
    }


    //UPDATE
    public void updateTravelRequest(Integer travelRequestId, TravelRequest travelRequest){

        TravelRequest oldTravelRequest = travelRequestRepository.findTravelRequestById(travelRequestId);

        // check if travel request exists
        if (oldTravelRequest == null) {
            throw new ApiException("travel request not found");
        }

        // completed or cancelled request cannot be updated
        if (oldTravelRequest.getStatus().equals("completed") || oldTravelRequest.getStatus().equals("cancelled")) {
            throw new ApiException("completed or cancelled travel request cannot be updated");
        }
        // cannot change family travel type while children are still linked
        if (oldTravelRequest.getTravelType().equals("family") && !travelRequest.getTravelType().equals("family") && childRepository.countChildByTravelRequest_Id(travelRequestId) > 0) {
            throw new ApiException("remove children before changing travel type from family");
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
        // any update makes the request incomplete again
        oldTravelRequest.setStatus("draft");
        travelRequestRepository.save(oldTravelRequest);
    }


    //DELETE
    public void deleteTravelRequest(Integer travelRequestId){

        TravelRequest travelRequest = travelRequestRepository.findTravelRequestById(travelRequestId);

        // check if travel request exists
        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }
        // cannot delete travel request if a trip was created from it
        if (travelRequest.getTrip() != null) {
            throw new ApiException("cannot delete travel request because it is linked to a trip");
        }

        travelRequestRepository.delete(travelRequest);
    }


    // SUBMIT TRAVEL REQUEST
    public void submitTravelRequest(Integer travelRequestId) {

        TravelRequest travelRequest = travelRequestRepository.findTravelRequestById(travelRequestId);

        // check if travel request exists
        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }

        // completed or cancelled request cannot be submitted
        if (travelRequest.getStatus().equals("completed") || travelRequest.getStatus().equals("cancelled")) {
            throw new ApiException("completed or cancelled travel request cannot be submitted");
        }

        // check if general preference is added
        if (travelRequest.getGeneralPreference() == null) {
            throw new ApiException("general preference is required");
        }

        // family travel must have at least one child
        if (travelRequest.getTravelType().equals("family") && (travelRequest.getChildren() == null || travelRequest.getChildren().isEmpty())) {
            throw new ApiException("family travel must include at least one child");
        }

        // request is complete and ready for recommendation
        travelRequest.setStatus("open");
        travelRequestRepository.save(travelRequest);
    }
}
