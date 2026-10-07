package com.example.sahldarbak.Controller;


import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.DTO.CreateCommunityPostDTO;
import com.example.sahldarbak.Model.CommunityPost;
import com.example.sahldarbak.Service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;


    // CREATE POST
    @PostMapping("/post/{tripId}")
    public ResponseEntity<ApiResponse> addPost(@PathVariable Integer tripId, @Valid @RequestBody CreateCommunityPostDTO dto) {
        communityService.addPost(tripId, dto);
        return ResponseEntity.status(200).body(new ApiResponse("Community post added successfully"));
    }


    // SEARCH BY COUNTRY
    @GetMapping("/country/{country}")
    public ResponseEntity<List<CommunityPost>> getPostsByCountry(@PathVariable String country) {
        return ResponseEntity.status(200).body(communityService.getPostsByCountry(country));
    }


    // GET ONE POST
    @GetMapping("/post/{postId}")
    public ResponseEntity<CommunityPost> getPost(@PathVariable Integer postId) {
        return ResponseEntity.status(200).body(communityService.getPost(postId));
    }
}
