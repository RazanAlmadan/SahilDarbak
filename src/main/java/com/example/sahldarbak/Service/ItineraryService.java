package com.example.sahldarbak.Service;


import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.SmartItinerary.ItineraryDayDTO;
import com.example.sahldarbak.DTO.SmartItinerary.PlaceRecommendationDTO;
import com.example.sahldarbak.DTO.SmartItinerary.SmartItineraryDTO;
import com.example.sahldarbak.Model.Itinerary;
import com.example.sahldarbak.Model.TripPlace;
import com.example.sahldarbak.Repository.ItineraryRepository;
import com.example.sahldarbak.Repository.TripPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final TripPlaceRepository tripPlaceRepository;
    private final ObjectMapper objectMapper;


    // ACCEPT SMART ITINERARY
    @Transactional
    public void acceptItinerary(Integer tripId) {

        Itinerary itinerary = itineraryRepository.findItineraryByTrip_Id(tripId);

        if (itinerary == null) {
            throw new ApiException("itinerary not found");
        }

        if (!itinerary.getStatus().equals("suggested")) {
            throw new ApiException("itinerary has already been accepted");
        }


        try {

            SmartItineraryDTO smartItinerary = objectMapper.readValue(itinerary.getPlanJson(), SmartItineraryDTO.class);


            if (smartItinerary.getDays() == null || smartItinerary.getDays().isEmpty()) {
                throw new ApiException("itinerary does not contain any days");
            }


            List<TripPlace> tripPlaces = new ArrayList<>();


            // CONVERT ACCEPTED DAILY PLAN TO TRIP PLACES
            for (ItineraryDayDTO day : smartItinerary.getDays()) {

                if (day.getPlaces() == null) {
                    continue;
                }

                for (PlaceRecommendationDTO place : day.getPlaces()) {

                    TripPlace tripPlace = new TripPlace();

                    tripPlace.setName(place.getName());
                    tripPlace.setPlaceType(place.getType());
                    tripPlace.setScheduledAt(day.getDate());
                    tripPlace.setCity(day.getCity());
                    tripPlace.setItinerary(itinerary);

                    tripPlaces.add(tripPlace);
                }
            }


            tripPlaceRepository.saveAll(tripPlaces);


            itinerary.setStatus("accepted");

            itineraryRepository.save(itinerary);


        } catch (ApiException e) {

            throw e;

        } catch (Exception e) {
            throw new ApiException("failed to accept itinerary: " + e.getMessage());
        }
    }


    // CANCEL SUGGESTED ITINERARY
    public void cancelItinerary(Integer tripId) {

        Itinerary itinerary = itineraryRepository.findItineraryByTrip_Id(tripId);

        if (itinerary == null) {
            throw new ApiException("itinerary not found");
        }

        if (!itinerary.getStatus().equals("suggested")) {
            throw new ApiException("accepted itinerary cannot be cancelled");
        }

        itineraryRepository.delete(itinerary);
    }
}