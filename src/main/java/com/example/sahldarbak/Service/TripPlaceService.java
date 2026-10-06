package com.example.sahldarbak.Service;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.Itinerary;
import com.example.sahldarbak.Model.TripPlace;
import com.example.sahldarbak.Repository.ItineraryRepository;
import com.example.sahldarbak.Repository.TripPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripPlaceService {

    private final TripPlaceRepository tripPlaceRepository;
    private final ItineraryRepository itineraryRepository;


    // GET ALL
    public List<TripPlace> getAllTripPlaces() {
        return tripPlaceRepository.findAll();
    }


    // ADD TRIP PLACE TO ITINERARY
    public void addTripPlace(Integer itineraryId, TripPlace tripPlace) {

        Itinerary itinerary = itineraryRepository.findItineraryById(itineraryId);

        if (itinerary == null) {
            throw new ApiException("itinerary not found");
        }

        if (!itinerary.getStatus().equals("accepted")) {
            throw new ApiException("trip places can only be added to an accepted itinerary");
        }
        tripPlace.setItinerary(itinerary);

        tripPlaceRepository.save(tripPlace);
    }


    // UPDATE
    public void updateTripPlace(Integer id, TripPlace tripPlace) {

        TripPlace oldTripPlace = tripPlaceRepository.findTripPlaceById(id);

        if (oldTripPlace == null) {
            throw new ApiException("trip place not found");
        }

        oldTripPlace.setName(tripPlace.getName());
        oldTripPlace.setPlaceType(tripPlace.getPlaceType());
        oldTripPlace.setScheduledAt(tripPlace.getScheduledAt());
        oldTripPlace.setNotes(tripPlace.getNotes());
        oldTripPlace.setCity(tripPlace.getCity());

        tripPlaceRepository.save(oldTripPlace);
    }


    // DELETE
    public void deleteTripPlace(Integer id) {

        TripPlace oldTripPlace = tripPlaceRepository.findTripPlaceById(id);

        if (oldTripPlace == null) {
            throw new ApiException("trip place not found");
        }

        tripPlaceRepository.delete(oldTripPlace);
    }

    //EXTRA ENDPOINTS

    // GET TRIP PLACES BY DATE
    public List<TripPlace> getTripPlacesByDate(Integer tripId, LocalDate date) {

        List<TripPlace> tripPlaces = tripPlaceRepository.findAllByItinerary_Trip_IdAndScheduledAt(tripId, date);

        if (tripPlaces.isEmpty()) {
            throw new ApiException("no trip places found for this date");
        }

        return tripPlaces;
    }

    // GET TODAY'S PLAN
    public List<TripPlace> getTodayPlan(Integer tripId) {
        LocalDate today = LocalDate.now();
        List<TripPlace> tripPlaces = tripPlaceRepository.findAllByItinerary_Trip_IdAndScheduledAt(tripId, today);

        if (tripPlaces.isEmpty()) {
            throw new ApiException("no places scheduled for today");
        }

        return tripPlaces;
    }
}