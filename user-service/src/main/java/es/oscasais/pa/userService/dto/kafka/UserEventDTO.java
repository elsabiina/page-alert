package es.oscasais.pa.userService.dto.kafka;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserEventDTO {

  @JsonProperty("userId")
  private UUID userId;

  @JsonProperty("email")
  private String email;

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
