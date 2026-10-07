package com.example.sahldarbak.Controller;

import com.example.sahldarbak.Api.ApiResponse;
import com.example.sahldarbak.DTO.LoginDTO;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/get")
    public ResponseEntity<?> get() {
        return ResponseEntity.status(200).body(userService.get());
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody @Valid User user) {
        userService.add(user);
        return ResponseEntity.status(200).body(new ApiResponse("user added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody @Valid User user) {
        userService.update(id, user);
        return ResponseEntity.status(200).body(new ApiResponse("user updated successfully") );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        userService.delete(id);
        return ResponseEntity.status(200).body(new ApiResponse("user deleted successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTO loginDTO) {
        User user = userService.login(loginDTO);
        return ResponseEntity.status(200).body(Map.of("userId", user.getId(), "email", user.getEmail()));
    }



}
