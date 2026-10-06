package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.TripBudgetEstimate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripBudgetEstimateRepository extends JpaRepository<TripBudgetEstimate, Integer> {
    TripBudgetEstimate findTripBudgetEstimateById(Integer id);
    TripBudgetEstimate findTripBudgetEstimateByTrip_Id(Integer tripId);

}
