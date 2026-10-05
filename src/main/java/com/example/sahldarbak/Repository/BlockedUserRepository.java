package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.BlockedUser;
import com.example.sahldarbak.Model.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser,Integer> {

    BlockedUser findBlockedUserById(Integer id);
    boolean existsByBlockerIdAndBlockedId(Integer blockerId, Integer blockedId);
    List<BlockedUser> findBlockedUsersByBlockerId(Integer blockerId);
}
