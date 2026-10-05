package com.example.sahldarbak.Service;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

// CRUD
    public List<User> get() {
        return userRepository.findAll();
    }
    public void add(User user) {

        if (userRepository.existsByEmail(user.getEmail()))
            throw new ApiException("email already occupied");

        if (userRepository.existsByPhoneNumber(user.getPhoneNumber()))
            throw new ApiException("phone number already occupied");

        user.setCreatedAt(LocalDate.now());
        user.setId(null);
        userRepository.save(user);
    }


    public void update(Integer id, User user) {
        User oldUser = userRepository.findUserById(id);
        if (oldUser == null)
            throw new ApiException("no such user exist");
        if (userRepository.existsByEmailAndIdNot(user.getEmail(), id)) {
            throw new ApiException("email is already registered");
        }
        if (userRepository.existsByPhoneNumberAndIdNot(user.getPhoneNumber(), id)) {
            throw new ApiException("phone number is already registered");
        }


        oldUser.setEmail(user.getEmail());
        oldUser.setPhoneNumber(user.getPhoneNumber());
        oldUser.setPassword(user.getPassword());


        userRepository.save(oldUser);
    }

    public void delete(Integer id) {
        User user = userRepository.findUserById(id);
        if (user == null)
            throw new ApiException("no such user exist");

        userRepository.delete(user);
    }

//    Extra endpoints


}
