package com.example.sahldarbak.Controller;


import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.Service.ItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/itinerary")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryService itineraryService;


    // ACCEPT AI ITINERARY
    @PostMapping("/accept/{tripId}")
    public ResponseEntity<?> acceptItinerary(@PathVariable Integer tripId) {
        itineraryService.acceptItinerary(tripId);
        return ResponseEntity.status(200).body(new ApiResponse("itinerary accepted successfully"));
    }


    // CANCEL SUGGESTED ITINERARY
    @DeleteMapping("/cancel/{tripId}")
    public ResponseEntity<?> cancelItinerary(@PathVariable Integer tripId) {
        itineraryService.cancelItinerary(tripId);
        return ResponseEntity.status(200).body(new ApiResponse("suggested itinerary cancelled successfully"));
    }

    // GET ITINERARY BY TRIP
    @GetMapping("/get-by-trip/{tripId}")
    public ResponseEntity<?> getItineraryByTrip(@PathVariable Integer tripId) {
        return ResponseEntity.status(200).body(itineraryService.getItineraryByTrip(tripId));
    }

    @PostMapping("/send-email/{tripId}")
    public ResponseEntity<?> sendItineraryToEmail(@PathVariable Integer tripId) {
        itineraryService.sendItineraryToEmail(tripId);
        return ResponseEntity.status(200).body(new ApiResponse("itinerary sent to email successfully"));
    }

    @PostMapping("/send-today-whatsapp/{tripId}")
    public ResponseEntity<?> sendTodayPlanToWhatsApp(@PathVariable Integer tripId) {

        itineraryService.sendTodayPlanToWhatsApp(tripId);

        return ResponseEntity.status(200).body(new ApiResponse("today's plan sent to WhatsApp successfully"));
    }
}