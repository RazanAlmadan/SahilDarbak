package com.example.sahldarbak.Controller;


import com.example.sahldarbak.AI.SmartItineraryAIService;
import com.example.sahldarbak.ExternalApi.GeoapifyService;
import com.example.sahldarbak.ExternalApi.TavilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/geoapify")
@RequiredArgsConstructor
public class GeoapifyController {

    private final GeoapifyService geoapifyService;
    private final TavilyService tavilyService;
    private final SmartItineraryAIService smartItineraryAIService;


    @GetMapping("/coordinates")
    public ResponseEntity<?> getCoordinates(
            @RequestParam String city,
            @RequestParam String country) {

        return ResponseEntity.status(200)
                .body(geoapifyService.getCoordinates(city, country));
    }


    @GetMapping("/hotels")
    public ResponseEntity<?> getHotels(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        return ResponseEntity.status(200)
                .body(geoapifyService.getHotels(latitude, longitude));
    }

    @GetMapping("/activities")
    public ResponseEntity<?> getActivities(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        return ResponseEntity.status(200)
                .body(geoapifyService.getActivities(latitude, longitude));
    }


    @GetMapping("/restaurants")
    public ResponseEntity<?> getRestaurants(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        return ResponseEntity.status(200)
                .body(geoapifyService.getRestaurants(latitude, longitude));
    }





}