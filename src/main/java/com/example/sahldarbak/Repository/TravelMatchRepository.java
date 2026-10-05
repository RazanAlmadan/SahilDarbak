package com.example.sahldarbak.Repository;

import com.example.sahldarbak.Model.Child;
import com.example.sahldarbak.Model.TravelMatch;
import com.example.sahldarbak.Model.TravelPresence;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelMatchRepository extends JpaRepository<TravelMatch,Integer> {
    TravelMatch findTravelMatchById(Integer id);
    boolean existsBySenderIdAndReceiverId(Integer senderId, Integer receiverId);

    boolean existsBySenderIdAndReceiverIdAndStatus(Integer senderId, Integer receiverId, String status);

    TravelMatch findTravelMatchBySenderIdAndReceiverIdAndStatus(Integer senderId, Integer receiverId, String status);

    List<TravelMatch> findTravelMatchesBySenderId(Integer senderId);
    List<TravelMatch> findTravelMatchesByReceiverId(Integer receiverId);

    List<TravelMatch> findTravelMatchesBySenderIdAndStatus(Integer senderId, String status);

    List<TravelMatch> findTravelMatchesByReceiverIdAndStatus(Integer receiverId, String status);

    long countByReceiverIdAndStatus(Integer receiverId, String status);
}
