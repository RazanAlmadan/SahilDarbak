package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.GeneralPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneralPreferenceRepository extends JpaRepository<GeneralPreference,Integer> {
    GeneralPreference findGeneralPreferenceById(Integer id);
    GeneralPreference findGeneralPreferenceByTravelRequest_Id(Integer travelRequestId);


}
