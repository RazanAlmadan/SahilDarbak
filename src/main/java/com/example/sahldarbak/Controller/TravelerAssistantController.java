package com.example.sahldarbak.Controller;

import com.example.sahldarbak.AI.TravelerAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assistant")
@RequiredArgsConstructor
public class TravelerAssistantController {

    private final TravelerAssistantService travelerAssistantService;

    @GetMapping("/city-guide/{userId}")
    public ResponseEntity<?> getCityGuide(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(travelerAssistantService.getCityGuide(userId));
    }
}