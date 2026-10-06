package com.example.sahldarbak.Service;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.SelectDestinationDTO;
import com.example.sahldarbak.DTO.SmartItinerary.HotelInsightDTO;
import com.example.sahldarbak.ExternalApi.GeoapifyService;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Model.Trip;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Repository.TravelRequestRepository;
import com.example.sahldarbak.Repository.TripRepository;
import com.example.sahldarbak.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.sahldarbak.AI.SmartItineraryAIService;
import com.example.sahldarbak.DTO.SmartItinerary.LocationDTO;
import com.example.sahldarbak.DTO.SmartItinerary.PlaceOptionDTO;
import com.example.sahldarbak.DTO.SmartItinerary.SmartItineraryDTO;
import com.example.sahldarbak.ExternalApi.TavilyService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TravelRequestRepository travelRequestRepository;
    private final GeoapifyService geoapifyService;
    private final TavilyService tavilyService;
    private final SmartItineraryAIService smartItineraryAIService;

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


    //EXTRA ENDPOINTS:


    // GENERATE SMART ITINERARY FOR TRIP
    public SmartItineraryDTO generateSmartItinerary(Integer tripId) {

        Trip trip = tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        TravelRequest travelRequest = trip.getTravelRequest();

        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }

        LocationDTO location = geoapifyService.getCoordinates(trip.getCity(), trip.getCountry());

        List<PlaceOptionDTO> hotels = geoapifyService.getHotels(location.getLatitude(), location.getLongitude());
        List<PlaceOptionDTO> activities = geoapifyService.getActivities(location.getLatitude(), location.getLongitude());
        List<PlaceOptionDTO> restaurants = geoapifyService.getRestaurants(location.getLatitude(), location.getLongitude());

        // ADD HOTEL RATINGS AND ESTIMATED PRICE
        for (PlaceOptionDTO hotel : hotels) {

            if (hotel.getName() == null) {
                continue;
            }
            HotelInsightDTO hotelInfo = tavilyService.getHotelInfo(hotel.getName(), trip.getCity(), trip.getCountry(), trip.getStartDate(), trip.getEndDate());

            hotel.setRatings(hotelInfo.getRatings());
            hotel.setEstimatedPrice(hotelInfo.getEstimatedPrice());


        }


        for (PlaceOptionDTO restaurant : restaurants) {

            if (restaurant.getName() == null) {
                continue;
            }

            restaurant.setHalalInfo(tavilyService.getRestaurantHalalInfo(restaurant.getName(), trip.getCity(), trip.getCountry()));
        }


        return smartItineraryAIService.generateSmartItinerary(travelRequest, hotels, activities, restaurants);
    }

    public void selectDestination(
            Integer user_id,
            Integer travel_request_id,
            SelectDestinationDTO dto
    ) {

        User user = userRepository.findUserById(user_id);

        if (user == null) {
            throw new ApiException("user not found");
        }

        TravelRequest travelRequest =
                travelRequestRepository.findTravelRequestById(travel_request_id);

        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }

        if (!travelRequest.getUser().getId().equals(user_id)) {
            throw new ApiException(
                    "this travel request does not belong to this user"
            );
        }

        if (travelRequest.getTrip() != null) {
            throw new ApiException(
                    "a trip has already been created for this travel request"
            );
        }

        Trip trip = new Trip();

        trip.setCountry(dto.getCountry());
        trip.setCity(dto.getCity());

        /*
         * Get these values directly from the travel request.
         */
        trip.setStartDate(travelRequest.getStartDate());
        trip.setEndDate(travelRequest.getEndDate());
        trip.setBudget(travelRequest.getBudget());

        /*
         * You can change this status depending on
         * how you want your existing trip workflow to work.
         */
        trip.setStatus("planned");

        trip.setUser(user);
        trip.setTravelRequest(travelRequest);

        tripRepository.save(trip);
    }


}
