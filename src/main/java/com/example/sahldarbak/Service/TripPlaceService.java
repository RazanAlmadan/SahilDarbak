package com.example.sahldarbak.Service;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.Trip;
import com.example.sahldarbak.Model.TripPlace;
import com.example.sahldarbak.Repository.TripPlaceRepository;
import com.example.sahldarbak.Repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripPlaceService {

    private final TripPlaceRepository tripPlaceRepository;
    private final TripRepository tripRepository;

    // get all
    public List<TripPlace> getAllTripPlaces(){
        return tripPlaceRepository.findAll();
    }

    // add (pass the trip_id to connect it with trip) (one-to-many)
    public void addTripPlace(Integer trip_id, TripPlace tripPlace){
        Trip trip = tripRepository.findTripById(trip_id);
        if (trip == null){
            throw new ApiException("trip not found");
        }
        tripPlace.setTrip(trip);
        tripPlaceRepository.save(tripPlace);
    }

    // update
    public void updateTripPlace(Integer id, TripPlace tripPlace){
        TripPlace oldTripPlace = tripPlaceRepository.findTripPlaceById(id);
        if (oldTripPlace == null){
            throw new ApiException("trip place not found");
        }
        oldTripPlace.setName(tripPlace.getName());
        oldTripPlace.setPlaceType(tripPlace.getPlaceType());
        oldTripPlace.setScheduledAt(tripPlace.getScheduledAt());
        oldTripPlace.setNotes(tripPlace.getNotes());
        tripPlaceRepository.save(oldTripPlace);
    }

    // delete
    public void deleteTripPlace(Integer id){
        TripPlace oldTripPlace = tripPlaceRepository.findTripPlaceById(id);
        if (oldTripPlace == null){
            throw new ApiException("trip place not found");
        }
        tripPlaceRepository.delete(oldTripPlace);
    }
}
