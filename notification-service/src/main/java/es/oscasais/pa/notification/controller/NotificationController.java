package es.oscasais.pa.notification.controller;

import es.oscasais.pa.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "API for managing email notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "Confirm email address")
    @GetMapping("/confirm-email/{token}")
    public ResponseEntity<String> confirmEmail(@PathVariable String token) {
        try {
            notificationService.confirmEmail(token);
            return ResponseEntity.ok("Email confirmed successfully! You can now log in to your account.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid or expired confirmation token.");
        }
    }

    @Operation(summary = "Clean up expired tokens")
    @PostMapping("/cleanup-expired-tokens")
    public ResponseEntity<String> cleanupExpiredTokens() {
        notificationService.cleanupExpiredTokens();
        return ResponseEntity.ok("Expired tokens cleaned up successfully.");
    }
}