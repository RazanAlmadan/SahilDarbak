package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.TravelRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TravelRestrictionRepository extends JpaRepository<TravelRestriction,Integer> {
    TravelRestriction findTravelRestrictionById(Integer id);

}
