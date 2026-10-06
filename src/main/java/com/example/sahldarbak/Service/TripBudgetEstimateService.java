package com.example.sahldarbak.Service;

import com.example.sahldarbak.AI.TripBudgetEstimateAIService;
import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.BudgetEstimateRequestDTO;
import com.example.sahldarbak.DTO.TripBudgetEstimateDTO;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Model.Trip;
import com.example.sahldarbak.Model.TripBudgetEstimate;
import com.example.sahldarbak.Model.TripCity;
import com.example.sahldarbak.Repository.TripBudgetEstimateRepository;
import com.example.sahldarbak.Repository.TripCityRepository;
import com.example.sahldarbak.Repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripBudgetEstimateService {

    private final TripBudgetEstimateRepository tripBudgetEstimateRepository;
    private final TripRepository tripRepository;
    private final TripBudgetEstimateAIService tripBudgetEstimateAIService;
    private final TripCityRepository tripCityRepository;

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


    //EXTRA ENDPOINT

    // GENERATE AI BUDGET ESTIMATE
    public TripBudgetEstimate generateBudgetEstimate(Integer tripId, BudgetEstimateRequestDTO requestDTO) {

        Trip trip = tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        TravelRequest travelRequest = trip.getTravelRequest();

        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }

        TripBudgetEstimate oldEstimate = tripBudgetEstimateRepository.findTripBudgetEstimateByTrip_Id(tripId);

        if (oldEstimate != null) {
            throw new ApiException("trip budget estimate already exists, use refresh instead");
        }


        List<TripCity> tripCities = getBudgetTripCities(tripId, travelRequest);


        TripBudgetEstimateDTO result = tripBudgetEstimateAIService.generateBudgetEstimate(trip, travelRequest, requestDTO, tripCities);


        validateBudgetResult(result);


        double total = result.getFlightEstimate() + result.getAccommodationEstimate() + result.getFoodEstimate() + result.getTransportationEstimate() + result.getActivitiesEstimate();


        TripBudgetEstimate estimate = new TripBudgetEstimate();

        estimate.setFlightEstimate(result.getFlightEstimate());
        estimate.setAccommodationEstimate(result.getAccommodationEstimate());
        estimate.setFoodEstimate(result.getFoodEstimate());
        estimate.setTransportationEstimate(result.getTransportationEstimate());
        estimate.setActivitiesEstimate(result.getActivitiesEstimate());

        estimate.setTotalEstimate(total);
        estimate.setCurrency(requestDTO.getCurrency());
        estimate.setSummary(result.getSummary());
        estimate.setGeneratedAt(LocalDateTime.now());

        estimate.setTrip(trip);


        return tripBudgetEstimateRepository.save(estimate);
    }


    // GET BUDGET ESTIMATE BY TRIP
    public TripBudgetEstimate getBudgetEstimateByTrip(Integer tripId) {

        Trip trip = tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        TripBudgetEstimate estimate = tripBudgetEstimateRepository.findTripBudgetEstimateByTrip_Id(tripId);

        if (estimate == null) {
            throw new ApiException("trip budget estimate not found");
        }

        return estimate;
    }


    // REFRESH AI BUDGET ESTIMATE
    public TripBudgetEstimate refreshBudgetEstimate(Integer tripId, BudgetEstimateRequestDTO requestDTO) {

        Trip trip = tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        TravelRequest travelRequest =
                trip.getTravelRequest();

        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }


        TripBudgetEstimate estimate = tripBudgetEstimateRepository.findTripBudgetEstimateByTrip_Id(tripId);

        if (estimate == null) {
            throw new ApiException("trip budget estimate not found");
        }


        List<TripCity> tripCities = getBudgetTripCities(tripId, travelRequest);


        TripBudgetEstimateDTO result = tripBudgetEstimateAIService.generateBudgetEstimate(trip, travelRequest, requestDTO, tripCities);


        validateBudgetResult(result);


        double total = result.getFlightEstimate() + result.getAccommodationEstimate() + result.getFoodEstimate() + result.getTransportationEstimate() + result.getActivitiesEstimate();


        estimate.setFlightEstimate(result.getFlightEstimate());
        estimate.setAccommodationEstimate(result.getAccommodationEstimate());
        estimate.setFoodEstimate(result.getFoodEstimate());
        estimate.setTransportationEstimate(result.getTransportationEstimate());
        estimate.setActivitiesEstimate(result.getActivitiesEstimate());

        estimate.setTotalEstimate(total);
        estimate.setCurrency(requestDTO.getCurrency());
        estimate.setSummary(result.getSummary());
        estimate.setGeneratedAt(LocalDateTime.now());


        return tripBudgetEstimateRepository.save(estimate);
    }

    //HELPER METHOD

    // GET CITIES NEEDED FOR BUDGET ESTIMATE
    private List<TripCity> getBudgetTripCities(Integer tripId, TravelRequest travelRequest) {

        if (travelRequest.getCityPlanMode().equals("single_city")) {

            return List.of();
        }


        List<TripCity> tripCities = tripCityRepository.findAllByTrip_IdAndStatusOrderByCityOrderAsc(tripId, "accepted");


        if (tripCities.isEmpty()) {
            throw new ApiException("accepted city plan is required before generating budget estimate");
        }

        return tripCities;
    }


    // VALIDATE AI BUDGET RESULT
    private void validateBudgetResult(TripBudgetEstimateDTO result) {

        if (result == null || result.getFlightEstimate() == null || result.getAccommodationEstimate() == null || result.getFoodEstimate() == null || result.getTransportationEstimate() == null || result.getActivitiesEstimate() == null) {

            throw new ApiException("AI returned incomplete budget estimate");
        }


        if (result.getFlightEstimate() < 0 || result.getAccommodationEstimate() < 0 || result.getFoodEstimate() < 0 || result.getTransportationEstimate() < 0 || result.getActivitiesEstimate() < 0) {

            throw new ApiException("AI returned invalid budget estimate");
        }
    }
}
