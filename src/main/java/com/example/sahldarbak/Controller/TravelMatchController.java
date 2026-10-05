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

    @PutMapping("/update/{inviteId}/{userId}/{status}")
    public ResponseEntity<?> update(@PathVariable Integer inviteId,
                                    @PathVariable Integer userId,
                                    @PathVariable String status) {
        travelMatchService.update(inviteId, userId, status);
        return ResponseEntity.status(200).body("invite updated successfully");
    }

    @GetMapping("/matches/{userId}")
    public ResponseEntity<?> getMatches(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(travelMatchService.getMatches(userId));
    }

    @GetMapping("/sent/{userId}")
    public ResponseEntity<?> getSent(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(travelMatchService.getSent(userId));
    }

    @GetMapping("/received/{userId}")
    public ResponseEntity<?> getReceived(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(travelMatchService.getReceived(userId));
    }

    @GetMapping("/accepted/{userId}")
    public ResponseEntity<?> getAccepted(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(travelMatchService.getAccepted(userId));
    }

    @GetMapping("/pending-count/{userId}")
    public ResponseEntity<?> getPendingCount(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(travelMatchService.getPendingCount(userId));
    }

    @GetMapping("/contact/{inviteId}/{userId}")
    public ResponseEntity<?> getContact(@PathVariable Integer inviteId, @PathVariable Integer userId) {
        return ResponseEntity.status(200).body(travelMatchService.getContact(inviteId, userId));
    }

    @GetMapping("/not-invited/{userId}")
    public ResponseEntity<?> getNotInvited(@PathVariable Integer userId) {
        return ResponseEntity.status(200).body(travelMatchService.getNotInvited(userId));
    }
}
