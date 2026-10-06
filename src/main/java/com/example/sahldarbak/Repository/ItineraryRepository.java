package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Integer> {

    Itinerary findItineraryById(Integer id);
    Itinerary findItineraryByTrip_Id(Integer tripId);

}