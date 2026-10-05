package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.Child;
import com.example.sahldarbak.Model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile,Integer> {
    Profile findProfileById(Integer id);
}
