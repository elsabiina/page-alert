package es.oscasais.pa.auth.dto;

import java.util.UUID;

/**
 * DTO for user information returned by /me endpoint
 */
public class UserInfoDTO {
    private final UUID id;
    private final String email;
    private final boolean emailConfirmed;

    public UserInfoDTO(UUID id, String email, boolean emailConfirmed) {
        this.id = id;
        this.email = email;
        this.emailConfirmed = emailConfirmed;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }
}