package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.FoodPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodPreferenceRepository extends JpaRepository<FoodPreference,Integer> {
    FoodPreference findFoodPreferenceById(Integer id);

}
