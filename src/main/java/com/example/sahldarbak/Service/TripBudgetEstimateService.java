package com.example.sahldarbak.Service;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.Trip;
import com.example.sahldarbak.Model.TripBudgetEstimate;
import com.example.sahldarbak.Repository.TripBudgetEstimateRepository;
import com.example.sahldarbak.Repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripBudgetEstimateService {

    public final TripBudgetEstimateRepository tripBudgetEstimateRepository;
    public final TripRepository tripRepository;

    // get
    public List<TripBudgetEstimate> getAllTripBudgetEstimates(){
        return tripBudgetEstimateRepository.findAll();
    }

    // add (pass the trip_id to connect them together) (one-to-one)
    public void addTripBudgetEstimate(Integer trip_id, TripBudgetEstimate tripBudgetEstimate){
        Trip trip = tripRepository.findTripById(trip_id);
        if (trip == null){
            throw new ApiException("trip not found");
        }
        tripBudgetEstimate.setTrip(trip);
        tripBudgetEstimateRepository.save(tripBudgetEstimate);
    }

    // update
    public void updateTripBudgetEstimate(Integer id, TripBudgetEstimate tripBudgetEstimate){
        TripBudgetEstimate oldTripBudgetEstimate = tripBudgetEstimateRepository.findTripBudgetEstimateById(id);
        if (oldTripBudgetEstimate == null){
            throw new ApiException("trip budget estimate not found");
        }
        oldTripBudgetEstimate.setAccommodationEstimate(tripBudgetEstimate.getAccommodationEstimate());
        oldTripBudgetEstimate.setActivitiesEstimate(tripBudgetEstimate.getActivitiesEstimate());
        oldTripBudgetEstimate.setFlightEstimate(tripBudgetEstimate.getFlightEstimate());
        oldTripBudgetEstimate.setFoodEstimate(tripBudgetEstimate.getFoodEstimate());
        oldTripBudgetEstimate.setTransportationEstimate(tripBudgetEstimate.getTransportationEstimate());
        oldTripBudgetEstimate.setTotalEstimate(tripBudgetEstimate.getTotalEstimate());
        oldTripBudgetEstimate.setGeneratedAt(tripBudgetEstimate.getGeneratedAt());
        tripBudgetEstimateRepository.save(oldTripBudgetEstimate);
    }

    // delete
    public void deleteTripBudgetEstimate(Integer id){
        TripBudgetEstimate oldTripBudgetEstimate = tripBudgetEstimateRepository.findTripBudgetEstimateById(id);
        if (oldTripBudgetEstimate == null){
            throw new ApiException("trip budget estimate not found");
        }
        tripBudgetEstimateRepository.delete(oldTripBudgetEstimate);
    }
}
