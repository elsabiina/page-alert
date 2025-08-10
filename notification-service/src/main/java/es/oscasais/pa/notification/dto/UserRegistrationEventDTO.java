package es.oscasais.pa.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class UserRegistrationEventDTO {
    @NotNull
    private UUID userId;

    @NotNull
    @Email
    private String email;

    public UserRegistrationEventDTO() {}

    public UserRegistrationEventDTO(UUID userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}