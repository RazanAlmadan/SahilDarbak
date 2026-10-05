package com.example.sahldarbak.Controller;

import com.example.sahldarbak.Service.TravelMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/travel-match")
@RequiredArgsConstructor
public class TravelMatchController {
    private final TravelMatchService travelMatchService;


//
    @PostMapping("/add/{senderId}/{receiverId}")
    public ResponseEntity<?> add(@PathVariable Integer senderId,
                                 @PathVariable Integer receiverId,
                                 @RequestParam String message) {
        travelMatchService.add(senderId, receiverId, message);
        return ResponseEntity.status(200).body("invite sent successfully");
    }

    @PutMapping("/update/{id}/{userId}/{status}")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @PathVariable Integer userId,
                                    @PathVariable String status) {
        travelMatchService.update(id, userId, status);
        return ResponseEntity.status(200).body("invite updated successfully");
    }

    @GetMapping("/matches/{userId}")
    public ResponseEntity<?> getMatches(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(travelMatchService.getMatches(userId));
    }
}
