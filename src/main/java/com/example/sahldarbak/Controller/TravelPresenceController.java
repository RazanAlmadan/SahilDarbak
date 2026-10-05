package com.example.sahldarbak.Controller;

import com.example.sahldarbak.DTO.TravelPresenceDTO;
import com.example.sahldarbak.Service.TravelPresenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/travelPresence")
@RequiredArgsConstructor
public class TravelPresenceController {
    private final TravelPresenceService travelPresenceService;

    @GetMapping("/get")
    public ResponseEntity<?> get() {
        return ResponseEntity.status(200).body(travelPresenceService.get());
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody @Valid TravelPresenceDTO dto) {
        travelPresenceService.add(dto);
        return ResponseEntity.status(200).body("travel presence added successfully");
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody @Valid TravelPresenceDTO dto) {
        travelPresenceService.update(dto);
        return ResponseEntity.status(200).body("travel presence updated successfully");
    }

    @DeleteMapping("/check-out/{userId}")
    public ResponseEntity<?> checkOut(@PathVariable Integer userId) {
        travelPresenceService.checkOut(userId);
        return ResponseEntity.status(200).body("checked out successfully");
    }
}
