package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.TripCity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripCityRepository extends JpaRepository<TripCity,Integer> {
    TripCity findTripCityById(Integer id);

    List<TripCity> findAllByTrip_IdOrderByCityOrderAsc(Integer tripId);

    boolean existsByTrip_IdAndCityOrder(
            Integer tripId,
            Integer cityOrder
    );

    boolean existsByTrip_IdAndCityOrderAndIdNot(
            Integer tripId,
            Integer cityOrder,
            Integer id
    );

    // GET CITIES BY STATUS
    List<TripCity> findAllByTrip_IdAndStatusOrderByCityOrderAsc(
            Integer tripId,
            String status
    );

    // CHECK IF TRIP HAS CITY PLAN WITH SPECIFIC STATUS
    boolean existsByTrip_IdAndStatus(
            Integer tripId,
            String status
    );
}
