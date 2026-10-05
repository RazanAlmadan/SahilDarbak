package com.example.sahldarbak.Service;

import com.example.sahldarbak.Api.ApiException;

import com.example.sahldarbak.DTO.TravelPresenceDTO;
import com.example.sahldarbak.Model.Profile;
import com.example.sahldarbak.Model.TravelPresence;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Repository.BlockedUserRepository;
import com.example.sahldarbak.Repository.ProfileRepository;
import com.example.sahldarbak.Repository.TravelPresenceRepository;
import com.example.sahldarbak.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelPresenceService {
    private final TravelPresenceRepository travelPresenceRepository;
    private final UserRepository userRepository;
    private final BlockedUserRepository blockedUserRepository;


//    return all
    public List<TravelPresence> get(){ return travelPresenceRepository.findAll();}



//    create a travel presence
    public void add(TravelPresenceDTO travelPresenceDTO){
        User user = userRepository.findUserById(travelPresenceDTO.getUserId());

        if (user == null)
            throw new ApiException("user not found");
        if (travelPresenceRepository.existsById(travelPresenceDTO.getUserId()))
            throw new ApiException("this user already has a travel presence");

        TravelPresence travelPresence= new TravelPresence();
        travelPresence.setCity(travelPresenceDTO.getCity().trim().toLowerCase());
        travelPresence.setCountry(travelPresenceDTO.getCountry().trim().toLowerCase());
        travelPresence.setUser(user);
        travelPresence.setCheckedInAt(LocalDate.now());
        travelPresenceRepository.save(travelPresence);

    }

//    update a travel presence
    public void update(TravelPresenceDTO presenceDTO) {
        TravelPresence presence = travelPresenceRepository.findTravelPresenceById(presenceDTO.getUserId());
        if (presence == null)
            throw new ApiException("user is not checked in anywhere");

        presence.setCountry(presenceDTO.getCountry().trim().toLowerCase());
        presence.setCity(presenceDTO.getCity().trim().toLowerCase());
        presence.setCheckedInAt(LocalDate.now());
        travelPresenceRepository.save(presence);
    }

//    check-out: removes the presence only to be null, the user stays
    @Transactional
    public void checkOut(Integer userId) {
        TravelPresence presence = travelPresenceRepository.findTravelPresenceById(userId);
        if (presence == null)
            throw new ApiException("user is not checked in anywhere");

        presence.getUser().setTravelPresence(null);
        travelPresenceRepository.delete(presence);
    }

    // Extra endpoints

    // number of travelers in my city, excluding me and blocked users
    public Integer getNearbyCount(Integer userId) {
        TravelPresence mine = travelPresenceRepository.findTravelPresenceById(userId);
        if (mine == null)
            throw new ApiException("check in to a city first");

        int count = 0;
        for (TravelPresence p : travelPresenceRepository.findByCountryAndCity(mine.getCountry(), mine.getCity())) {
            Integer otherId = p.getId();
            if (otherId.equals(userId))
                continue;
            if (blockedUserRepository.existsByBlockerIdAndBlockedId(userId, otherId)
                    || blockedUserRepository.existsByBlockerIdAndBlockedId(otherId, userId))
                continue;
            count++;
        }
        return count;
    }



}
