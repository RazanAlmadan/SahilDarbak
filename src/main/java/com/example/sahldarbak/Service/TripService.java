package com.example.sahldarbak.Service;

import com.example.sahldarbak.AI.SmartCityPlannerAIService;
import com.example.sahldarbak.AI.TransportationAIService;
import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.CityPlan.CityPlanDTO;
import com.example.sahldarbak.DTO.CityPlan.CityPlanItemDTO;
import com.example.sahldarbak.AI.PackingListAIService;
import com.example.sahldarbak.DTO.DestinationRecommendation.HolidayDTO;
import com.example.sahldarbak.DTO.DestinationRecommendation.HolidayInfoDTO;
import com.example.sahldarbak.DTO.DestinationRecommendation.SelectDestinationDTO;
import com.example.sahldarbak.DTO.PackingList.PackingListDTO;
import com.example.sahldarbak.DTO.SmartItinerary.HotelInsightDTO;
import com.example.sahldarbak.DTO.TransportationRouteDTO;
import com.example.sahldarbak.ExternalApi.*;
import com.example.sahldarbak.Model.*;
import com.example.sahldarbak.Repository.TravelRequestRepository;
import com.example.sahldarbak.Repository.TripCityRepository;
import com.example.sahldarbak.Repository.TripRepository;
import com.example.sahldarbak.Repository.UserRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.sahldarbak.AI.SmartItineraryAIService;
import com.example.sahldarbak.DTO.SmartItinerary.LocationDTO;
import com.example.sahldarbak.DTO.SmartItinerary.PlaceOptionDTO;
import com.example.sahldarbak.DTO.SmartItinerary.SmartItineraryDTO;
import com.example.sahldarbak.DTO.SmartItinerary.ItineraryDayDTO;
import com.example.sahldarbak.DTO.SmartItinerary.PlaceRecommendationDTO;
import com.example.sahldarbak.Repository.ItineraryRepository;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
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
    private final TripPlaceService tripPlaceService;
    private final TransportationAIService transportationAIService;
    private final WeatherService weatherService;
    private final HolidayService holidayService;
    private final CountryService countryService;

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
    public void selectDestination(Integer user_id, Integer travel_request_id, SelectDestinationDTO dto) {

        User user = userRepository.findUserById(user_id);

        if (user == null) {
            throw new ApiException("user not found");
        }

        TravelRequest travelRequest = travelRequestRepository.findTravelRequestById(travel_request_id);

        if (travelRequest == null) {
            throw new ApiException("travel request not found");
        }

        if (!travelRequest.getUser().getId().equals(user_id)) {
            throw new ApiException("this travel request does not belong to this user");
        }

        if (travelRequest.getTrip() != null) {
            throw new ApiException("a trip has already been created for this travel request");
        }
        // create new Trip with the country the user chose and TravelRequest info
        Trip trip = new Trip();

        trip.setCountry(dto.getCountry());
        trip.setCity(dto.getCity());



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

    /// help user create a packing list
    public PackingListDTO generatePackingList(Integer tripId) {

        Trip trip = tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        return packingListAIService.generatePackingList(trip);
    }

    /// use trip id to get all trip places info
    public List<TransportationRouteDTO> generateTransportation(Integer tripId) {

        Trip trip = tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        List<TripPlace> tripPlaces = tripPlaceService.getTripPlacesByTrip(tripId);

        List<TransportationRouteDTO> routes = new ArrayList<>();

        // get info from trip place

        for (int i = 0; i < tripPlaces.size() - 1; i++) {

            TripPlace from = tripPlaces.get(i);

            TripPlace to = tripPlaces.get(i + 1);

            // Only connect places on the same day.
            if (!from.getScheduledAt().equals(to.getScheduledAt())) {
                continue;
            }

            TransportationRouteDTO route = transportationAIService.generateTransportation(from, to, trip.getCountry(), trip.getTravelRequest());

            routes.add(route);
        }


        if (routes.isEmpty()) {
            throw new ApiException("not enough trip places on the same day to generate transportation");
        }

        return routes;
    }
    /// check the weather in your trip
    public Object getTripWeather(Integer tripId) {

        Trip trip = tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        return weatherService.getWeather(trip.getCity());
    }
    /// check if there is holidays in your trip
    public HolidayInfoDTO getTripHolidays(Integer tripId) {

        Trip trip = tripRepository.findTripById(tripId);

        if (trip == null) {
            throw new ApiException("trip not found");
        }

        if (trip.getTravelRequest() == null) {
            throw new ApiException("travel request not found");
        }
        //get start date from travel request
        LocalDate startDate = trip.getTravelRequest().getStartDate();
        //get end date from travel request
        LocalDate endDate = trip.getTravelRequest().getEndDate();

        if (startDate == null || endDate == null) {
            throw new ApiException("trip dates are required");
        }

        String countryCode = countryService.getCountryCode(trip.getCountry());

        JsonArray holidays = holidayService.getHolidays(countryCode, startDate.getYear());

        List<HolidayDTO> holidaysDuringTrip = new ArrayList<>();

        for (int i = 0; i < holidays.size(); i++) {

            JsonObject holiday = holidays.get(i).getAsJsonObject();

            LocalDate holidayDate = LocalDate.parse(holiday.get("date").getAsString());

            if (!holidayDate.isBefore(startDate) && !holidayDate.isAfter(endDate)) {

                HolidayDTO holidayDTO = new HolidayDTO();

                holidayDTO.setName(holiday.get("localName").getAsString());

                holidayDTO.setDate(holiday.get("date").getAsString());

                holidayDTO.setType(holiday.get("types").toString());

                holidaysDuringTrip.add(holidayDTO);
            }
        }
        return new HolidayInfoDTO(!holidaysDuringTrip.isEmpty(), holidaysDuringTrip.size(), holidaysDuringTrip);
    }



}