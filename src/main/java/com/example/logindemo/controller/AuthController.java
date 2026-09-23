package com.example.logindemo.controller;

import com.example.logindemo.config.JwtUtil;
import com.example.logindemo.config.LoginAttemptService;
import com.example.logindemo.model.LoginRequest;
import com.example.logindemo.model.RegisterRequest;
import com.example.logindemo.model.Users;
import com.example.logindemo.repository.UserRepository;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;

    public AuthController(PasswordEncoder passwordEncoder, UserRepository userRepository, JwtUtil jwtUtil, LoginAttemptService loginAttemptService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.loginAttemptService = loginAttemptService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "409", description = "Username already taken"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody Users user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username already taken"));
        }

        String rawPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole("USER");
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully!"));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT returned"),
            @ApiResponse(responseCode = "401", description = "Incorrect password"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "423", description = "Account temporarily locked")
    })

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody Users loginData) {

        if (loginAttemptService.isBlocked(loginData.getUsername())) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(Map.of("error", "Account temporarily locked due to too many failed attempts. Try again in a few minutes."));
        }

        Users existingUser = userRepository.findByUsername(loginData.getUsername());

        if (existingUser != null) {

            boolean matches = passwordEncoder.matches(loginData.getPassword(), existingUser.getPassword());

            if (matches) {
                loginAttemptService.loginSucceeded(existingUser.getUsername());
                String token = jwtUtil.generateToken(existingUser.getUsername(), existingUser.getRole());
                return ResponseEntity.ok(Map.of("token", token));
            } else {
                loginAttemptService.loginFailed(loginData.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Incorrect Password!"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found! please register first!"));
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        String username = auth.getName();
        return ResponseEntity.ok(Map.of("message", "Welcome to your protectect file, " + username + "!"));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin dashboard accessed successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient role — ADMIN only")
    })

    @GetMapping("/admin/dashboard")
    public ResponseEntity<?> getAdminDashboard() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(Map.of("message", "Welcome to the Admin Dashboard! " + username + "!"));
    }

}
