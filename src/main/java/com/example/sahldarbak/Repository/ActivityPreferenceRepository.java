package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.ActivityPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityPreferenceRepository extends JpaRepository<ActivityPreference,Integer> {
    ActivityPreference findActivityPreferenceById(Integer id);

}
