package es.oscasais.pa.auth.dto.kafka;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class EmailConfirmationEventDTO {
  @NotNull
  @Email
  private String email;

  @NotNull
  private Instant confirmedAt;

  public EmailConfirmationEventDTO() {
  }

  public EmailConfirmationEventDTO(String email, Instant confirmedAt) {
    this.email = email;
    this.confirmedAt = confirmedAt;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Instant getConfirmedAt() {
    return confirmedAt;
  }

  public void setConfirmedAt(Instant confirmedAt) {
    this.confirmedAt = confirmedAt;
  }
}
