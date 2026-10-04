package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChildRepository extends JpaRepository<Child,Integer> {
    Child findChildById(Integer id);
    Integer countChildByTravelRequest_Id(Integer travelRequestId);
}
