package com.example.sahldarbak.Service;


import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.Trip;
import com.example.sahldarbak.Model.TripCity;
import com.example.sahldarbak.Repository.TripCityRepository;
import com.example.sahldarbak.Repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripCityService {
    private final TripCityRepository tripCityRepository;
    private final TripRepository tripRepository;


    //GET
    public List<TripCity>getTripCities(){
        return tripCityRepository.findAll();
    }

    // GET CITIES FOR SPECIFIC TRIP ORDERED BY CITY ORDER
    public List<TripCity> getTripCitiesByTripId(Integer tripId) {

        Trip trip = tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        return tripCityRepository.findAllByTrip_IdOrderByCityOrderAsc(tripId);
    }

    // ADD TRIP CITY
    public void addTripCity(Integer tripId, TripCity tripCity) {

        Trip trip = tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        if (!trip.getTravelRequest().getCityPlanMode().equals("multi_city_manual")) {
            throw new ApiException("city plan mode must be multi_city_manual");
        }

        if (tripCity.getEndDate().isBefore(tripCity.getStartDate())) {
            throw new ApiException("end date must be after or equal to start date");
        }

        if (tripCity.getStartDate().isBefore(trip.getTravelRequest().getStartDate()) || tripCity.getEndDate().isAfter(trip.getTravelRequest().getEndDate())) {

            throw new ApiException("city dates must be within trip dates");
        }

        List<TripCity> tripCities = tripCityRepository.findAllByTrip_IdOrderByCityOrderAsc(tripId);

        for (TripCity city : tripCities) {

            if (city.getCityOrder().equals(tripCity.getCityOrder())) {
                throw new ApiException("city order already exists");
            }

            if (!tripCity.getEndDate().isBefore(city.getStartDate())
                    && !tripCity.getStartDate().isAfter(city.getEndDate())) {

                throw new ApiException("city dates overlap with another city");
            }
        }

        tripCity.setTrip(trip);
        tripCity.setStatus("accepted");
        tripCityRepository.save(tripCity);
    }
    // UPDATE TRIP CITY
    public void updateTripCity(Integer id, TripCity tripCity) {

        TripCity oldTripCity = tripCityRepository.findTripCityById(id);

        if (oldTripCity == null) {
            throw new ApiException("trip city not found");
        }

        if (oldTripCity.getStatus().equals("suggested")) {
            throw new ApiException("suggested city plan cannot be updated");
        }


        Trip trip = oldTripCity.getTrip();

        if (tripCity.getEndDate().isBefore(tripCity.getStartDate())) {
            throw new ApiException("end date must be after or equal to start date");
        }

        if (tripCity.getStartDate().isBefore(trip.getTravelRequest().getStartDate()) || tripCity.getEndDate().isAfter(trip.getTravelRequest().getEndDate())) {

            throw new ApiException("city dates must be within trip dates");
        }

        List<TripCity> tripCities = tripCityRepository.findAllByTrip_IdOrderByCityOrderAsc(trip.getId());

        for (TripCity city : tripCities) {

            if (city.getId().equals(id)) {
                continue;
            }

            if (city.getCityOrder().equals(tripCity.getCityOrder())) {
                throw new ApiException("city order already exists");
            }

            if (!tripCity.getEndDate().isBefore(city.getStartDate()) && !tripCity.getStartDate().isAfter(city.getEndDate())) {

                throw new ApiException("city dates overlap with another city");
            }
        }

        oldTripCity.setCity(tripCity.getCity());
        oldTripCity.setStartDate(tripCity.getStartDate());
        oldTripCity.setEndDate(tripCity.getEndDate());
        oldTripCity.setCityOrder(tripCity.getCityOrder());

        tripCityRepository.save(oldTripCity);
    }

    // DELETE TRIP CITY
    public void deleteTripCity(Integer id) {

        TripCity tripCity = tripCityRepository.findTripCityById(id);
        if (tripCity == null) {
            throw new ApiException("trip city not found");
        }

        if (tripCity.getStatus().equals("suggested")) {
            throw new ApiException("suggested city plan cannot be deleted individually");
        }

        tripCityRepository.delete(tripCity);
    }


    //EXTRA ENDPOINT

    // ACCEPT AI CITY PLAN
    public void acceptCityPlan(Integer tripId) {

        Trip trip = tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        List<TripCity> suggestedCities = tripCityRepository.findAllByTrip_IdAndStatusOrderByCityOrderAsc(tripId, "suggested");

        if (suggestedCities.isEmpty()) {
            throw new ApiException("there is no suggested city plan to accept");
        }

        if (tripCityRepository.existsByTrip_IdAndStatus(tripId, "accepted")) {

            throw new ApiException("city plan has already been accepted");
        }

        for (TripCity tripCity : suggestedCities) {
            tripCity.setStatus("accepted");
        }

        tripCityRepository.saveAll(suggestedCities);
    }


    // CANCEL SUGGESTED CITY PLAN
    public void cancelCityPlan(Integer tripId) {

        Trip trip = tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        List<TripCity> suggestedCities = tripCityRepository.findAllByTrip_IdAndStatusOrderByCityOrderAsc(tripId, "suggested");

        if (suggestedCities.isEmpty()) {
            throw new ApiException("there is no suggested city plan");
        }

        tripCityRepository.deleteAll(suggestedCities);
    }

}
