package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<Trip, Integer> {
    Trip findTripById(Integer id);
}
