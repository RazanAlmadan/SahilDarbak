package com.example.sahldarbak.Service;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.TravelPresenceDTO;
import com.example.sahldarbak.Model.TravelPresence;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Repository.BlockedUserRepository;
import com.example.sahldarbak.Repository.ProfileRepository;
import com.example.sahldarbak.Repository.TravelPresenceRepository;
import com.example.sahldarbak.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.sahldarbak.DTO.NearestTravelerDTO;
import com.example.sahldarbak.Model.Profile;
import com.example.sahldarbak.Repository.ProfileRepository;

import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelPresenceService {
    private final TravelPresenceRepository travelPresenceRepository;
    private final UserRepository userRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final GeocodingService geocodingService;
    private final ProfileRepository profileRepository;

    // return all
    public List<TravelPresence> get() { return travelPresenceRepository.findAll(); }

    // create a travel presence, country and city come from the coordinates
    public void add(TravelPresenceDTO dto) {
        User user = userRepository.findUserById(dto.getUserId());
        if (user == null)
            throw new ApiException("user not found");
        if (travelPresenceRepository.existsById(dto.getUserId()))
            throw new ApiException("this user already has a travel presence");

        String[] location = geocodingService.reverseGeocode(dto.getLatitude(), dto.getLongitude());

        TravelPresence travelPresence = new TravelPresence();
        travelPresence.setCountry(location[0]);
        travelPresence.setCity(location[1]);
        travelPresence.setLatitude(dto.getLatitude());
        travelPresence.setLongitude(dto.getLongitude());
        travelPresence.setCheckedInAt(LocalDate.now());
        travelPresence.setUser(user);
        travelPresenceRepository.save(travelPresence);
    }

    // update a travel presence
    public void update(TravelPresenceDTO dto) {
        TravelPresence presence = travelPresenceRepository.findTravelPresenceById(dto.getUserId());
        if (presence == null)
            throw new ApiException("user is not checked in anywhere");

        String[] location = geocodingService.reverseGeocode(dto.getLatitude(), dto.getLongitude());

        presence.setCountry(location[0]);
        presence.setCity(location[1]);
        presence.setLatitude(dto.getLatitude());
        presence.setLongitude(dto.getLongitude());
        presence.setCheckedInAt(LocalDate.now());
        travelPresenceRepository.save(presence);
    }

    // check-out: removes the presence only, the user stays
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

    // travelers in my city, nearest first, with the distance in km
    // travelers in my city, nearest first, with the distance in km
    public List<NearestTravelerDTO> getNearest(Integer userId) {
        TravelPresence mine = travelPresenceRepository.findTravelPresenceById(userId);
        if (mine == null)
            throw new ApiException("check in to a city first");

        List<NearestTravelerDTO> result = new ArrayList<>();
        for (TravelPresence p : travelPresenceRepository.findByCountryAndCity(mine.getCountry(), mine.getCity())) {
            Integer otherId = p.getId();
            if (otherId.equals(userId))
                continue;
            if (blockedUserRepository.existsByBlockerIdAndBlockedId(userId, otherId)
                    || blockedUserRepository.existsByBlockerIdAndBlockedId(otherId, userId))
                continue;

            Profile other = profileRepository.findProfileById(otherId);
            if (other == null)
                continue;

            int age = Period.between(other.getDateOfBirth(), LocalDate.now()).getYears();
            double km = distanceKm(mine.getLatitude(), mine.getLongitude(), p.getLatitude(), p.getLongitude());

            result.add(new NearestTravelerDTO(
                    otherId,
                    other.getFullName(),
                    age,
                    other.getGender(),
                    other.getCountry(),
                    other.getCity(),
                    other.getBio(),
                    p.getCountry(),
                    p.getCity(),
                    Math.round(km * 10) / 10.0));
        }
        result.sort(Comparator.comparing(NearestTravelerDTO::getDistanceKm));
        return result;
    }

    // Haversine formula, distance between two points in km
    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}