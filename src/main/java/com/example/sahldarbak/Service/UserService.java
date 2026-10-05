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

    //    get all users
    public List<User> get() {
        return userRepository.findAll();
    }

    //    add a user
    public void add(User user) {

//   exceptions
        if (userRepository.existsByEmail(user.getEmail()))
            throw new ApiException("email already occupied");

        if (userRepository.existsByPhoneNumber(user.getPhoneNumber()))
            throw new ApiException("phone number already occupied");
//        manual date setting
        user.setCreatedAt(LocalDate.now());

        userRepository.save(user);
    }


    public void update(Integer id, User user) {

//        access old user
        User oldUser = userRepository.findUserById(id);

//        validate existance of old user
        if (oldUser == null)
            throw new ApiException("no such user exist");

//        validate the new user
        if (userRepository.existsByEmailAndIdNot(user.getEmail(), id)) {
            throw new ApiException("email is already registered");
        }
        if (userRepository.existsByPhoneNumberAndIdNot(user.getPhoneNumber(), id)) {
            throw new ApiException("phone number is already registered");
        }

        // update the olduser
        oldUser.setEmail(user.getEmail());
        oldUser.setPhoneNumber(user.getPhoneNumber());
        oldUser.setPassword(user.getPassword());

//        save updates
        userRepository.save(oldUser);
    }

    public void delete(Integer id) {
        User user = userRepository.findUserById(id);
        if (user == null)
            throw new ApiException("no such user exist");

        userRepository.delete(user);
    }


}
