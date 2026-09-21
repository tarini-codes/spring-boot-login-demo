package com.example.logindemo.controller;

import com.example.logindemo.config.JwtUtil;
import com.example.logindemo.model.Users;
import com.example.logindemo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;
    public AuthController(PasswordEncoder passwordEncoder, UserRepository userRepository, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public String registerUser(@Valid @RequestBody Users user) {
        String rawPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));

        user.setRole("USER");

        userRepository.save(user);
        return "User registered successfully";
    }

    @PostMapping("/login")
    public String loginUser(@Valid @RequestBody Users loginData) {
        Users existingUser = userRepository.findByUsername(loginData.getUsername());

        if (existingUser != null) {

            boolean matches = passwordEncoder.matches(loginData.getPassword(), existingUser.getPassword());

            if (matches) {
                String token = jwtUtil.generateToken(existingUser.getUsername(), existingUser.getRole());
                return token;
            } else {
                return "Error: Incorrect Password!";
            }
        } else {
            return "Error: User not found! please register first!";
        }
    }

    @GetMapping("/admin/dashboard")
    public String getAdminDashboard() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return "Welcome to the Admin Dashboard, " + username + "!";
    }

    @GetMapping("/profile")
    public String getProfile() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "Error: Not authenticated";
        }
        String username = auth.getName();
        return "Welcome to your protected profile, " + username + "!";
    }
}
