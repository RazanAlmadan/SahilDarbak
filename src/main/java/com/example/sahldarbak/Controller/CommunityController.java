package com.example.sahldarbak.Controller;


import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.DTO.Post.CreateCommunityPostDTO;
import com.example.sahldarbak.Service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllPosts(){
        return ResponseEntity.status(200).body(communityService.getAllPosts());
    }


    // CREATE POST
    @PostMapping("/post/{user_id}")
    public ResponseEntity<?> addPost(@PathVariable Integer user_id, @Valid @RequestBody CreateCommunityPostDTO dto) {
        communityService.addPost(user_id, dto);
        return ResponseEntity.status(200).body(new ApiResponse("Community post added successfully"));
    }


    // SEARCH BY COUNTRY
    @GetMapping("/country/{country}")
    public ResponseEntity<?> getPostsByCountry(@PathVariable String country) {
        return ResponseEntity.status(200).body(communityService.getPostsByCountry(country));
    }


    // GET ONE POST
    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Integer postId) {
        return ResponseEntity.status(200).body(communityService.getPost(postId));
    }

    @GetMapping("/get/by/rating")
    public ResponseEntity<?> getPostsByRating(){
        return ResponseEntity.status(200).body(communityService.getPostsByRating());
    }

    @GetMapping("/get/by/name/and/rating/{country}")
    public ResponseEntity<?> getPostsByNameAndRating(@PathVariable String country){
        return ResponseEntity.status(200).body(communityService.getPostByNameAndRating(country));
    }


}
