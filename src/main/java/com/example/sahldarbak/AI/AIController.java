package com.example.sahldarbak.AI;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIDestinationRecommendationService aiService;

    @GetMapping("/get/Generated/countries/{travel_request_id}")
    public ResponseEntity<?> generateCountry(@PathVariable Integer travel_request_id){
        return ResponseEntity.status(200).body(aiService.generateCountry(travel_request_id));
    }

    @PostMapping("/compare-countries")
    public ResponseEntity<?> compareCountries(@RequestParam Integer travelRequestId, @RequestParam String country1, @RequestParam String country2) {
        return ResponseEntity.status(200).body(aiService.compareCountries(travelRequestId, country1, country2));
    }

}
