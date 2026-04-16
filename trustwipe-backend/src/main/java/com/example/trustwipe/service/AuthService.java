package com.example.trustwipe.service;

import com.example.trustwipe.dto.AuthRequest;
import com.example.trustwipe.dto.AuthResponse;
import com.example.trustwipe.model.User;
import com.example.trustwipe.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service to handle authentication and user registration logic.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user if the email doesn't exist.
     */
    public AuthResponse register(AuthRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                return new AuthResponse("Error: Email is already in use!", false);
            }

            User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                LocalDateTime.now()
            );

            userRepository.save(user);

            return new AuthResponse("User registered successfully!", true);
        } catch (Exception e) {
            return new AuthResponse("Error: Database connection failed. " + e.getMessage(), false);
        }
    }

    /**
     * Authenticates a user based on email and password.
     */
    public AuthResponse login(AuthRequest request) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                    return new AuthResponse("Login successful!", UUID.randomUUID().toString(), user.getEmail(), true);
                }
            }

            return new AuthResponse("Error: Invalid email or password", false);
        } catch (Exception e) {
            return new AuthResponse("Error: Database connection failed. " + e.getMessage(), false);
        }
    }
}
