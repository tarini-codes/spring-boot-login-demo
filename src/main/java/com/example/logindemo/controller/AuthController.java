package com.example.logindemo.controller;


import com.example.logindemo.model.Users;
import com.example.logindemo.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody Users user) {
        userRepository.save(user);
        return "User registered successfully";
    }

    @PostMapping("/login")
    public String loginUser(@RequestBody Users loginData) {
        Users existingUser = userRepository.findByUsername(loginData.getUsername());

        if(existingUser != null) {
            if(existingUser.getPassword().equals(loginData.getPassword())) {
                return "Login successful! Welcome " + existingUser.getUsername();
            } else {
                return "Error: Incorrect Password!";
            }
        } else {
            return "Error: user not found! please register first";
        }

    }
}
