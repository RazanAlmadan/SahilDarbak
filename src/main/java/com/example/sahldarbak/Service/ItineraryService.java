package com.example.sahldarbak.Service;


import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.ItineraryResponseDTO;
import com.example.sahldarbak.DTO.SmartItinerary.ItineraryDayDTO;
import com.example.sahldarbak.DTO.SmartItinerary.PlaceRecommendationDTO;
import com.example.sahldarbak.DTO.SmartItinerary.SmartItineraryDTO;
import com.example.sahldarbak.ExternalApi.EmailService;
import com.example.sahldarbak.ExternalApi.WhatsAppService;
import com.example.sahldarbak.Model.Itinerary;
import com.example.sahldarbak.Model.TripPlace;
import com.example.sahldarbak.Repository.ItineraryRepository;
import com.example.sahldarbak.Repository.TripPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final TripPlaceRepository tripPlaceRepository;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;


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
                    tripPlace.setOfficialWebsite(place.getOfficialWebsite());

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

    // GET ITINERARY BY TRIP
    public ItineraryResponseDTO getItineraryByTrip(Integer tripId) {

        Itinerary itinerary = itineraryRepository.findItineraryByTrip_Id(tripId);

        if (itinerary == null) {
            throw new ApiException("itinerary not found");
        }

        try {

            SmartItineraryDTO plan = objectMapper.readValue(itinerary.getPlanJson(), SmartItineraryDTO.class);

            return new ItineraryResponseDTO(itinerary.getId(), itinerary.getStatus(), itinerary.getGeneratedAt(), plan);

        } catch (Exception e) {
            throw new ApiException("failed to read itinerary: " + e.getMessage());
        }
    }

    public void sendItineraryToEmail(Integer tripId, String lang) {

        Itinerary itinerary = itineraryRepository.findItineraryByTrip_Id(tripId);

        if (itinerary == null) {
            throw new ApiException("itinerary not found");
        }

        try {

            SmartItineraryDTO smartItinerary = objectMapper.readValue(itinerary.getPlanJson(), SmartItineraryDTO.class);

            emailService.sendFullItineraryEmail(itinerary.getTrip().getUser().getEmail(), itinerary.getTrip().getCountry(), smartItinerary, lang);
        } catch (Exception e) {

            throw new ApiException("failed to send itinerary email: " + e.getMessage());
        }
    }

    // SEND TODAY'S CURRENT PLAN TO WHATSAPP
    @Transactional(readOnly = true)
    public void sendTodayPlanToWhatsApp(Integer tripId) {

        Itinerary itinerary = itineraryRepository.findItineraryByTrip_Id(tripId);

        if (itinerary == null) {
            throw new ApiException("itinerary not found");
        }

        if (!itinerary.getStatus().equals("accepted")) {
            throw new ApiException("itinerary must be accepted before sending today's plan");
        }


        LocalDate today = LocalDate.now();


        List<TripPlace> todayPlaces = tripPlaceRepository.findAll().stream().filter(place -> place.getItinerary() != null
                                        && place.getItinerary().getId().equals(itinerary.getId())
                                        && today.equals(place.getScheduledAt())).toList();


        if (todayPlaces.isEmpty()) {

            throw new ApiException("no itinerary plan found for today");
        }


        StringBuilder message = new StringBuilder();


        message.append("Your SahlDarbak plan for today ✈️\n\n");

        message.append("📅 ").append(today).append("\n\n");


        String currentCity = null;


        for (TripPlace place : todayPlaces) {

            if (place.getCity() != null && !place.getCity().equals(currentCity)) {

                currentCity = place.getCity();

                message.append("📍 ").append(currentCity).append("\n");
            }


            message.append("• ").append(place.getName());


            if (place.getPlaceType() != null) {

                message.append(" (").append(place.getPlaceType()).append(")");
            }


            if (place.getNotes() != null && !place.getNotes().isBlank()) {

                message.append(" - ").append(place.getNotes());
            }


            message.append("\n");
        }


        message.append("\nHave a great day! 💛");


        whatsAppService.sendText(itinerary.getTrip().getUser().getPhoneNumber(), message.toString());
    }
}