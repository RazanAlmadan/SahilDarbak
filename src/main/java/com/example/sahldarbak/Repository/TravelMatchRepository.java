package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.Child;
import com.example.sahldarbak.Model.TravelMatch;
import com.example.sahldarbak.Model.TravelPresence;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelMatchRepository extends JpaRepository<TravelMatch,Integer> {
    TravelMatch findTravelMatchById(Integer id);
}
