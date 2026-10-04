package com.example.sahldarbak.Service;

import com.example.sahldarbak.Model.ActivityPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityPreferenceRepository extends JpaRepository<ActivityPreference,Integer> {
    ActivityPreference findActivityPreferenceById(Integer id);

}
