package com.example.sahldarbak.Service;

import com.example.sahldarbak.AI.SmartCityPlannerAIService;
import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.CityPlan.CityPlanDTO;
import com.example.sahldarbak.DTO.CityPlan.CityPlanItemDTO;
import com.example.sahldarbak.AI.PackingListAIService;
import com.example.sahldarbak.DTO.DestinationRecommendation.SelectDestinationDTO;
import com.example.sahldarbak.DTO.PackingList.PackingListDTO;
import com.example.sahldarbak.DTO.SmartItinerary.HotelInsightDTO;
import com.example.sahldarbak.ExternalApi.EmailService;
import com.example.sahldarbak.ExternalApi.GeoapifyService;
import com.example.sahldarbak.Model.TravelRequest;
import com.example.sahldarbak.Model.Trip;
import com.example.sahldarbak.Model.TripCity;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Repository.TravelRequestRepository;
import com.example.sahldarbak.Repository.TripCityRepository;
import com.example.sahldarbak.Repository.TripRepository;
import com.example.sahldarbak.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.sahldarbak.AI.SmartItineraryAIService;
import com.example.sahldarbak.DTO.SmartItinerary.LocationDTO;
import com.example.sahldarbak.DTO.SmartItinerary.PlaceOptionDTO;
import com.example.sahldarbak.DTO.SmartItinerary.SmartItineraryDTO;
import com.example.sahldarbak.ExternalApi.TavilyService;
import com.example.sahldarbak.DTO.SmartItinerary.ItineraryDayDTO;
import com.example.sahldarbak.DTO.SmartItinerary.PlaceRecommendationDTO;
import com.example.sahldarbak.Model.Itinerary;
import com.example.sahldarbak.Repository.ItineraryRepository;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import java.util.ArrayList;

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
    private final SmartCityPlannerAIService smartCityPlannerAIService;
    private final TripCityRepository tripCityRepository;
    private final ItineraryRepository itineraryRepository;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final PackingListAIService packingListAIService;

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
        oldTrip.setCity(trip.getCity());
        oldTrip.setCountry(trip.getCountry());

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

        Itinerary oldItinerary = itineraryRepository.findItineraryByTrip_Id(tripId);

        if (oldItinerary != null && oldItinerary.getStatus().equals("accepted")) {
            throw new ApiException("accepted itinerary cannot be regenerated");
        }


        // FINAL RESULT
        SmartItineraryDTO finalItinerary = new SmartItineraryDTO();

        List<ItineraryDayDTO> allDays = new ArrayList<>();
        List<PlaceRecommendationDTO> allHotels = new ArrayList<>();
        List<PlaceRecommendationDTO> allRestaurants = new ArrayList<>();
        List<PlaceRecommendationDTO> allActivities = new ArrayList<>();


        // SINGLE CITY
        if (travelRequest.getCityPlanMode().equals("single_city")) {

            SmartItineraryDTO cityItinerary = generateItineraryForCity(travelRequest, trip.getCountry(), trip.getCity(), travelRequest.getStartDate(), travelRequest.getEndDate());

            // ADD CITY TO EACH DAY
            if (cityItinerary.getDays() != null) {
                for (ItineraryDayDTO day : cityItinerary.getDays()) {
                    day.setCity(trip.getCity());
                    allDays.add(day);
                }
            }

            if (cityItinerary.getHotelRecommendations() != null) {
                allHotels.addAll(cityItinerary.getHotelRecommendations());
            }

            if (cityItinerary.getRestaurantRecommendations() != null) {
                allRestaurants.addAll(cityItinerary.getRestaurantRecommendations());
            }

            if (cityItinerary.getActivityRecommendations() != null) {
                allActivities.addAll(cityItinerary.getActivityRecommendations());
            }
        }


        // MULTI CITY MANUAL OR AI
        else if (travelRequest.getCityPlanMode().equals("multi_city_manual") || travelRequest.getCityPlanMode().equals("multi_city_ai")) {

            List<TripCity> tripCities = tripCityRepository.findAllByTrip_IdAndStatusOrderByCityOrderAsc(tripId, "accepted");

            if (tripCities.isEmpty()) {
                throw new ApiException("accepted city plan is required before generating itinerary");
            }


            for (TripCity tripCity : tripCities) {

                SmartItineraryDTO cityItinerary = generateItineraryForCity(travelRequest, trip.getCountry(), tripCity.getCity(), tripCity.getStartDate(), tripCity.getEndDate());


                // ADD CITY TO EACH DAY
                if (cityItinerary.getDays() != null) {
                    for (ItineraryDayDTO day : cityItinerary.getDays()) {

                        day.setCity(tripCity.getCity());

                        allDays.add(day);
                    }
                }


                if (cityItinerary.getHotelRecommendations() != null) {
                    allHotels.addAll(cityItinerary.getHotelRecommendations());
                }

                if (cityItinerary.getRestaurantRecommendations() != null) {
                    allRestaurants.addAll(cityItinerary.getRestaurantRecommendations());
                }

                if (cityItinerary.getActivityRecommendations() != null) {
                    allActivities.addAll(cityItinerary.getActivityRecommendations());
                }
            }
        }

        else {
            throw new ApiException("invalid city plan mode");
        }


        // RESET DAY NUMBERS AFTER COMBINING CITY PLANS
        for (int i = 0; i < allDays.size(); i++) {
            allDays.get(i).setDayNumber(i + 1);
        }


        finalItinerary.setDays(allDays);
        finalItinerary.setHotelRecommendations(allHotels);
        finalItinerary.setRestaurantRecommendations(allRestaurants);
        finalItinerary.setActivityRecommendations(allActivities);


        try {

            String planJson = objectMapper.writeValueAsString(finalItinerary);
            Itinerary itinerary = itineraryRepository.findItineraryByTrip_Id(tripId);


            boolean firstGeneration = itinerary == null;

            // FIRST GENERATION
            if (itinerary == null) {

                itinerary = new Itinerary();

                itinerary.setTrip(trip);
            }


            // NEW OR REGENERATED SUGGESTED PLAN
            itinerary.setStatus("suggested");
            itinerary.setGeneratedAt(LocalDateTime.now());
            itinerary.setPlanJson(planJson);

            itineraryRepository.save(itinerary);
            if (firstGeneration) {
                emailService.sendFullItineraryEmail(trip.getUser().getEmail(), trip.getCountry(), finalItinerary);
            }


            return finalItinerary;

        } catch (Exception e) {

            throw new ApiException("failed to save smart itinerary: " + e.getMessage());
        }
    }



    // after AI suggest countries the user can select one
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
        // create new Trip with the country the user chose and TravelRequest info
        Trip trip = new Trip();

        trip.setCountry(dto.getCountry());
        trip.setCity(dto.getCity());


        // Get these values directly from the travel request.

//        trip.setStartDate(travelRequest.getStartDate());
//        trip.setEndDate(travelRequest.getEndDate());
//        trip.setBudget(travelRequest.getBudget());


        // we can change it if we want
        trip.setStatus("planned");

        trip.setUser(user);
        trip.setTravelRequest(travelRequest);

        tripRepository.save(trip);
    }

    // GENERATE AI CITY PLAN
    public CityPlanDTO generateCityPlan(Integer tripId) {

        Trip trip = tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        TravelRequest travelRequest = trip.getTravelRequest();

        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }

        if (!travelRequest.getCityPlanMode().equals("multi_city_ai")) {
            throw new ApiException("city plan mode must be multi_city_ai");
        }

        // if user already accepted a plan, do not generate another one
        if (tripCityRepository.existsByTrip_IdAndStatus(tripId, "accepted")) {
            throw new ApiException("city plan has already been accepted");
        }

        // generate new AI city plan
        CityPlanDTO cityPlan = smartCityPlannerAIService.generateCityPlan(trip, travelRequest);

        if (cityPlan.getCities() == null || cityPlan.getCities().isEmpty()) {
            throw new ApiException("AI did not generate any cities");
        }

        // remove previous suggested plan before saving new one
        List<TripCity> oldSuggestedCities = tripCityRepository.findAllByTrip_IdAndStatusOrderByCityOrderAsc(tripId, "suggested");

        tripCityRepository.deleteAll(oldSuggestedCities);

        // save AI result as suggested
        for (CityPlanItemDTO cityDTO : cityPlan.getCities()) {

            TripCity tripCity = new TripCity();

            tripCity.setCity(cityDTO.getCity());
            tripCity.setStartDate(cityDTO.getStartDate());
            tripCity.setEndDate(cityDTO.getEndDate());
            tripCity.setCityOrder(cityDTO.getCityOrder());
            tripCity.setStatus("suggested");
            tripCity.setTrip(trip);

            tripCityRepository.save(tripCity);
        }

        return cityPlan;
    }

    // GET TRIP BY TRAVEL REQUEST
    public Trip getTripByTravelRequest(Integer travelRequestId) {

        Trip trip = tripRepository.findTripByTravelRequest_Id(travelRequestId);

        if (trip == null) {
            throw new ApiException("trip not found for this travel request");
        }

        return trip;
    }


    //HELPER METHOD
    // GENERATE ITINERARY FOR ONE CITY
    private SmartItineraryDTO generateItineraryForCity(TravelRequest travelRequest, String country, String city, java.time.LocalDate startDate, java.time.LocalDate endDate) {


        LocationDTO location = geoapifyService.getCoordinates(city, country);


        List<PlaceOptionDTO> hotels = geoapifyService.getHotels(location.getLatitude(), location.getLongitude());

        List<PlaceOptionDTO> activities = geoapifyService.getActivities(location.getLatitude(), location.getLongitude());

        List<PlaceOptionDTO> restaurants = geoapifyService.getRestaurants(location.getLatitude(), location.getLongitude());


        // ADD HOTEL RATINGS AND ESTIMATED PRICE
        for (PlaceOptionDTO hotel : hotels) {

            if (hotel.getName() == null) {
                continue;
            }

            HotelInsightDTO hotelInfo = tavilyService.getHotelInfo(hotel.getName(), city, country, startDate, endDate);

            hotel.setRatings(hotelInfo.getRatings());
            hotel.setEstimatedPrice(hotelInfo.getEstimatedPrice());
        }


        // ADD RESTAURANT HALAL INFO
        for (PlaceOptionDTO restaurant : restaurants) {

            if (restaurant.getName() == null) {
                continue;
            }

            restaurant.setHalalInfo(tavilyService.getRestaurantHalalInfo(restaurant.getName(), city, country));
        }


        return smartItineraryAIService.generateSmartItinerary(travelRequest, city, startDate, endDate, hotels, activities, restaurants);
    }


    public PackingListDTO generatePackingList(Integer tripId) {

        Trip trip =
                tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        return packingListAIService.generatePackingList(trip);
    }


}