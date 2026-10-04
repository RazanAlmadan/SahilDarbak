package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.TravelRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelRequestRepository extends JpaRepository<TravelRequest,Integer> {
    TravelRequest findTravelRequestById(Integer id);

}
