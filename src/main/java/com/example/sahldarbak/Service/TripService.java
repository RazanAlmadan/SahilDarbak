package com.example.sahldarbak.Service;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Model.Trip;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Repository.TravelRequestRepository;
import com.example.sahldarbak.Repository.TripRepository;
import com.example.sahldarbak.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TravelRequestRepository travelRequestRepository;

    public List<Trip> getAllTrips(){
        return tripRepository.findAll();
    }

    /// add (pass to it the user_id and the travel_request_id to link them together)

    public void addTrip(Integer user_id, Integer travel_request_id, Trip trip){
         User user = userRepository.findUserById(user_id);
         if (user == null){
         throw new ApiException("user not found");
         }
        TravelRequest travelRequest = travelRequestRepository.findTravelRequestById(travel_request_id);
        if (travelRequest == null){
            throw new ApiException("travel request not found");
        }
        trip.setUser(user);
        trip.setTravelRequest(travelRequest);
        tripRepository.save(trip);
    }

    // update
    public void updateTrip(Integer id, Trip trip){
        Trip oldTrip = tripRepository.findTripById(id);
        if (oldTrip == null){
            throw new ApiException("trip not found");
        }
        oldTrip.setBudget(trip.getBudget());
        oldTrip.setCity(trip.getCity());
        oldTrip.setCountry(trip.getCountry());
        oldTrip.setEndDate(trip.getEndDate());
        oldTrip.setStartDate(trip.getStartDate());
        oldTrip.setStatus(trip.getStatus());
        oldTrip.setTravelType(trip.getTravelType());
        tripRepository.save(oldTrip);
    }

    // delete
    public void deleteTrip(Integer id){
        Trip oldTrip = tripRepository.findTripById(id);
        if (oldTrip == null){
            throw new ApiException("trip not found");
        }
        tripRepository.delete(oldTrip);
    }
}
