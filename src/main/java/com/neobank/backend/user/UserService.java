package com.neobank.backend.user;

import com.neobank.backend.auth.UserRepository;
import com.neobank.backend.entity.User;
import com.neobank.backend.exception.ResourceNotFoundException;
import com.neobank.backend.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException("User not found: " + email));
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String email,
                                             UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException("User not found: " + email));

        user.setFullName(request.getFullName());
        return UserProfileResponse.from(userRepository.save(user));
    }

    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserProfileResponse::from)
                .collect(Collectors.toList());
    }
}