package com.example.sahldarbak.Service;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.ProfileDTO;
import com.example.sahldarbak.DTO.TravelPresenceDTO;
import com.example.sahldarbak.Model.TravelPresence;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Repository.TravelPresenceRepository;
import com.example.sahldarbak.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelPresenceService {
    private final TravelPresenceRepository travelPresenceRepository;
    private final UserRepository userRepository;

    public List<TravelPresence> get(){ return travelPresenceRepository.findAll();}

    public void add(TravelPresenceDTO travelPresenceDTO){
        User user = userRepository.findUserById(travelPresenceDTO.getUserId());

        if (user == null)
            throw new ApiException("user not found");
        if (travelPresenceRepository.existsById(travelPresenceDTO.getUserId()))
            throw new ApiException("this user already has a travel presence");

        TravelPresence travelPresence= new TravelPresence();
        travelPresence.setCity(travelPresenceDTO.getCity());
        travelPresence.setCountry(travelPresenceDTO.getCountry());
        travelPresence.setUser(user);
        travelPresenceRepository.save(travelPresence);

    }

    public void update(TravelPresenceDTO presenceDTO) {
        TravelPresence presence = travelPresenceRepository.findTravelPresenceById(presenceDTO.getUserId());
        if (presence == null)
            throw new ApiException("user is not checked in anywhere");

        presence.setCountry(presenceDTO.getCountry());
        presence.setCity(presenceDTO.getCity());

        travelPresenceRepository.save(presence);
    }

//    no delete, deletion occur only from the user service side.

    @Transactional
    public void checkOut(Integer userId) {
        TravelPresence presence = travelPresenceRepository.findTravelPresenceById(userId);
        if (presence == null)
            throw new ApiException("user is not checked in anywhere");

        presence.getUser().setTravelPresence(null);
        travelPresenceRepository.delete(presence);
    }

}
