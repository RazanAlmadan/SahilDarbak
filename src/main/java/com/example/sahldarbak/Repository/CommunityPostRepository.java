package com.example.sahldarbak.Repository;


import com.example.sahldarbak.Model.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Integer> {

    CommunityPost findCommunityPostById(Integer id);
    List<CommunityPost> findAllByCountryIgnoreCase(String country);
}
