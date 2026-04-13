package com.neobank.backend.auth;

import com.neobank.backend.auth.dto.*;
import com.neobank.backend.entity.User;
import com.neobank.backend.exception.EmailAlreadyExistsException;
import com.neobank.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil              jwtUtil;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        return RegisterResponse.from(userRepository.save(user));
    }

    public LoginResponse login(LoginRequest request) {
        try {
            // 1. Authenticate the user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));

        } catch (DisabledException e) {
            // 2. Catch specifically so it reaches GlobalExceptionHandler as a 403
            throw new DisabledException("Account is inactive");

        } catch (AuthenticationException e) {
            // 3. Catch wrong passwords/emails as a 401
            throw new BadCredentialsException("Invalid email or password");
        }

        // 4. If authentication passes, fetch the user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // 5. Generate the JWT Token
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name());

        // 6. Return the response
        return LoginResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }
}