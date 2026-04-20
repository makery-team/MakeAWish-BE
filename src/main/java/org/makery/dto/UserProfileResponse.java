package org.makery.dto;

import org.makery.domain.Language;
import org.makery.domain.User;
import org.makery.domain.UserRole;

public record UserProfileResponse(
        Long id,
        String email,
        String name,
        String nickname,
        String phoneNumber,
        Language language,
        UserRole userRole
) {
    /**
     * Entity -> DTO 변환을 위한 정적 팩토리 메서드
     */
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getPhoneNumber(),
                user.getLanguage(),
                user.getUserRole()
        );
    }
}
