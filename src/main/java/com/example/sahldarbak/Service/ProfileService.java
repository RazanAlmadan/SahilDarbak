package com.example.sahldarbak.Service;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.ProfileDTO;
import com.example.sahldarbak.Model.Profile;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Repository.ProfileRepository;
import com.example.sahldarbak.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

//    CRUD without delete
    public List<Profile> get(){ return profileRepository.findAll();}
    public void add(ProfileDTO profileDTO){
        User user = userRepository.findUserById(profileDTO.getUserId());

        if (user == null)
            throw new ApiException("user not found");
        if (profileRepository.existsById(profileDTO.getUserId()))
            throw new ApiException("this user already has a profile");

        Profile profile = new Profile();
        profile.setFullName(profileDTO.getFullName());
        profile.setDateOfBirth(profileDTO.getDateOfBirth());
        profile.setGender(profileDTO.getGender());
        profile.setCountry(profileDTO.getCountry());
        profile.setCity(profileDTO.getCity());
        profile.setBio(profileDTO.getBio());
        profile.setUser(user);

        profileRepository.save(profile);


    }
    public void update(ProfileDTO profileDTO){
        Profile profile = profileRepository.findProfileById(profileDTO.getUserId());

        if (profile == null)
            throw new ApiException("profile not found");

        profile.setFullName(profileDTO.getFullName());
        profile.setDateOfBirth(profileDTO.getDateOfBirth());
        profile.setGender(profileDTO.getGender());
        profile.setCountry(profileDTO.getCountry());
        profile.setCity(profileDTO.getCity());
        profile.setBio(profileDTO.getBio());
        profileRepository.save(profile);
    }

//    no delete method as user can not delete his profile

// Extra endpoints

}
