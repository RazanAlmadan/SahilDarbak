package com.example.sahldarbak.Service;


import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.CreateCommunityPostDTO;
import com.example.sahldarbak.Model.CommunityPost;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Repository.CommunityPostRepository;
import com.example.sahldarbak.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    public List<CommunityPost> getAllPosts(){
        return communityPostRepository.findAll();
    }

    public void addPost(Integer userId, CreateCommunityPostDTO dto) {

        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("user not found");
        }

        CommunityPost post = new CommunityPost();

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCountry(dto.getCountry());
        post.setCity(dto.getCity());
        post.setUser(user);

        communityPostRepository.save(post);
    }

    public List<CommunityPost> getPostsByCountry(String country) {
        List<CommunityPost> posts = communityPostRepository.findAllByCountryIgnoreCase(country);
        if (posts.isEmpty()){
            throw new ApiException("no posts found");
        }
        return posts;
    }

    public CommunityPost getPost(Integer postId) {
        CommunityPost post = communityPostRepository.findCommunityPostById(postId);
        if (post == null){
            throw new ApiException("post not found");
        }
        return post;
    }
}