package com.example.sahldarbak.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripBudgetEstimate extends JpaRepository<TripBudgetEstimate, Integer> {
    TripBudgetEstimate findTripBudgetEstimateById(Integer id);
}
