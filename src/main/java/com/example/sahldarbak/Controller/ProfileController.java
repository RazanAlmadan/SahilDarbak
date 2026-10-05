package com.example.sahldarbak.Controller;

import com.example.sahldarbak.DTO.ProfileDTO;
import com.example.sahldarbak.Service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody @Valid ProfileDTO dto) {
        profileService.add(dto);
        return ResponseEntity.status(200).body("profile added successfully");
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody @Valid ProfileDTO dto) {
        profileService.update(dto);
        return ResponseEntity.status(200).body("profile updated successfully");
    }

}
