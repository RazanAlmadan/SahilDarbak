package com.example.sahldarbak.Controller;

import com.example.sahldarbak.Service.BlockedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/blocked-user")
@RequiredArgsConstructor
public class BlockedUserController {

    private final BlockedUserService blockedUserService;

// CRUD without update as block action only needs removing
    @GetMapping("/get")
    public ResponseEntity<?> get() {
        return ResponseEntity.status(200).body(blockedUserService.get());
    }

    @PostMapping("/add/{blockerId}/{blockedId}")
    public ResponseEntity<?> add(@PathVariable Integer blockerId, @PathVariable Integer blockedId) {
        blockedUserService.add(blockerId, blockedId);
        return ResponseEntity.status(200).body("user blocked successfully");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        blockedUserService.delete(id);
        return ResponseEntity.status(200).body("block record deleted successfully");
    }
}
