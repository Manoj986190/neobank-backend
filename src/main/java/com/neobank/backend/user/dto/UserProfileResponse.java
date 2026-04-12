package com.neobank.backend.user.dto;

import com.neobank.backend.entity.User;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {

    private Long          id;
    private String        fullName;
    private String        email;
    private String        role;
    private Boolean       isActive;
    private LocalDateTime createdAt;

    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}