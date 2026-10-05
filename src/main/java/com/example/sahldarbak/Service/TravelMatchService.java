package com.example.sahldarbak.Service;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.Profile;
import com.example.sahldarbak.Model.TravelMatch;
import com.example.sahldarbak.Model.TravelPresence;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelMatchService {
    private final TravelMatchRepository travelMatchRepository;
    private final UserRepository userRepository;
    private final TravelPresenceRepository travelPresenceRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final ProfileRepository profileRepository;

//    return the profiles of people in which they are in the same city and country the user is in.
public List<Profile> getMatches(Integer userId) {
    TravelPresence mine = travelPresenceRepository.findTravelPresenceById(userId);
    if (mine == null)
        throw new ApiException("check in to a city first");

    List<Integer> ids = new ArrayList<>();
    for (TravelPresence p : travelPresenceRepository.findByCountryAndCity(mine.getCountry(), mine.getCity())) {
        Integer otherId = p.getId();
        if (otherId.equals(userId))
            continue;
        if (blockedUserRepository.existsByBlockerIdAndBlockedId(userId, otherId)
                || blockedUserRepository.existsByBlockerIdAndBlockedId(otherId, userId))
            continue;
        ids.add(otherId);
    }
    return profileRepository.findAllById(ids);
}


    // send an invite after you see the profiles given from the TravelPresence getMatches method
    public void add(Integer senderId, Integer receiverId, String message) {
        if (senderId.equals(receiverId))
            throw new ApiException("you cannot invite yourself");

        if (message == null || message.isBlank() || message.length() > 300)
            throw new ApiException("message is required and must not exceed 300 characters");

        User sender = userRepository.findUserById(senderId);
        if (sender == null)
            throw new ApiException("sender user not found");

        User receiver = userRepository.findUserById(receiverId);
        if (receiver == null)
            throw new ApiException("receiver user not found");

        if (travelMatchRepository.existsBySenderIdAndReceiverId(senderId, receiverId))
            throw new ApiException("you already sent an invite to this user");

        if (travelMatchRepository.existsBySenderIdAndReceiverIdAndStatus(receiverId, senderId, "pending"))
            throw new ApiException("this user already sent you a pending invite, respond to it instead");

        TravelMatch match = new TravelMatch();
        match.setSender(sender);
        match.setReceiver(receiver);
        match.setMessage(message);
        match.setStatus("pending");
        match.setCreatedAt(LocalDate.now());

        travelMatchRepository.save(match);
    }

    // respond to an invite: only the status changes
    public void update(Integer id, String status) {
        TravelMatch match = travelMatchRepository.findTravelMatchById(id);
        if (match == null)
            throw new ApiException("invite not found");

        if (!status.equals("accepted") && !status.equals("rejected"))
            throw new ApiException("status must be accepted or rejected");

        if (!match.getStatus().equals("pending"))
            throw new ApiException("this invite was already answered");

        match.setStatus(status);
        travelMatchRepository.save(match);
    }

    // no delete: a rejected row must stay, and rows are removed by the user-delete cascade
}
