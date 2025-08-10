package es.oscasais.pa.notification.repository;

import es.oscasais.pa.notification.model.EmailConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, UUID> {
    Optional<EmailConfirmationToken> findByToken(String token);
    Optional<EmailConfirmationToken> findByUserEmailAndConfirmedFalse(String userEmail);
    List<EmailConfirmationToken> findByExpiresAtBeforeAndConfirmedFalse(Instant now);
    void deleteByExpiresAtBeforeAndConfirmedFalse(Instant now);
}