package com.neobank.backend.auth;

import com.neobank.backend.auth.dto.*;
import com.neobank.backend.entity.User;
import com.neobank.backend.exception.EmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        log.debug("Registration attempt for email: {}", request.getEmail());

        // BR-01: Email must be unique
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // BR-06: Never store plain text — always BCrypt
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        User saved = userRepository.save(user);
        log.debug("User registered successfully with id: {}", saved.getId());

        return RegisterResponse.from(saved);
    }
}