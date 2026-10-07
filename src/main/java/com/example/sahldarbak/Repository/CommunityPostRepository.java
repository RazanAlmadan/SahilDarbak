package com.example.sahldarbak.Repository;


import com.example.sahldarbak.Model.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Integer> {

    CommunityPost findCommunityPostById(Integer id);
    List<CommunityPost> findAllByCountryIgnoreCase(String country);

    @Query("SELECT p from CommunityPost p ORDER BY rating DESC ")
    List<CommunityPost> getPostByRatings();

    @Query("SELECT p FROM CommunityPost p WHERE LOWER(p.country) = LOWER(?1) ORDER BY p.rating DESC ")
    List<CommunityPost> getPostByRatingsAndCountry(String country);
}
