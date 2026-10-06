package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.TripPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TripPlaceRepository extends JpaRepository<TripPlace, Integer> {
    TripPlace findTripPlaceById(Integer id);
    List<TripPlace> findAllByItinerary_Trip_IdAndScheduledAt(Integer tripId, LocalDate scheduledAt);
    List<TripPlace> findAllByItinerary_Trip_Id(Integer tripId);
}
