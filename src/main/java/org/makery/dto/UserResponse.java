package org.makery.dto;

import lombok.Getter;
import org.makery.domain.User;

@Getter
public class UserResponse {

    private final String email;
    private final String name;
    private final Long userId;

    public UserResponse(User user) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.userId = user.getId();
    }
}
