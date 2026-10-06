package com.example.sahldarbak.AI;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @GetMapping("/get/Generated/countries/{travel_request_id}")
    public ResponseEntity<?> generateCountry(@PathVariable Integer travel_request_id){
        return ResponseEntity.status(200).body(aiService.generateCountry(travel_request_id));
    }

}
