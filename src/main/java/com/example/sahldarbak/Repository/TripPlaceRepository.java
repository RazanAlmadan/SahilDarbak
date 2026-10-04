package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.TripPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripPlaceRepository extends JpaRepository<TripPlace, Integer> {
    TripPlace findTripPlaceById(Integer id);
}
