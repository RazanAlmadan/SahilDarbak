package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.Child;
import com.example.sahldarbak.Model.TravelPresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelPresenceRepository extends JpaRepository<TravelPresence,Integer> {

    TravelPresence findTravelPresenceById(Integer id);
    List<TravelPresence> findByCountryAndCity(String country, String city);

}
