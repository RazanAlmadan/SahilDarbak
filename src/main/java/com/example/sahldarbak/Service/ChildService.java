package com.example.sahldarbak.Service;


import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.Child;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Repository.ChildRepository;
import com.example.sahldarbak.Repository.TravelRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChildService {


    private final ChildRepository childRepository;
    private final TravelRequestRepository travelRequestRepository;


    // GET ALL
    public List<Child> getChildren() {
        return childRepository.findAll();
    }


    // ADD
    public void addChild(Integer travelRequestId, Child child) {

        TravelRequest travelRequest = travelRequestRepository.findTravelRequestById(travelRequestId);

        // check if travel request exists
        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }

        // children can only be added to family travel
        if (!travelRequest.getTravelType().equals("family")) {
            throw new ApiException("children can only be added to family travel");
        }

        // link child to travel request
        child.setTravelRequest(travelRequest);
        childRepository.save(child);
    }


    // UPDATE
    public void updateChild(Integer childId, Child child) {

        Child oldChild = childRepository.findChildById(childId);

        // check if child exists
        if (oldChild == null) {
            throw new ApiException("child not found");
        }

        oldChild.setAge(child.getAge());
        childRepository.save(oldChild);
    }


    // DELETE
    public void deleteChild(Integer childId) {

        Child child = childRepository.findChildById(childId);

        // check if child exists
        if (child == null) {
            throw new ApiException("child not found");
        }

        TravelRequest travelRequest = child.getTravelRequest();

        // submitted family request must have at least one child
        if (travelRequest.getStatus().equals("open") && childRepository.countChildByTravelRequest_Id(travelRequest.getId()) == 1) {

            throw new ApiException("family travel must have at least one child");
        }

        childRepository.delete(child);
    }
}
