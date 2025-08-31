package es.oscasais.pa.userService.service;

import es.oscasais.pa.userService.dto.UserResponseDTO;
import es.oscasais.pa.userService.exception.UserNotFoundException;
import es.oscasais.pa.userService.model.User;
import es.oscasais.pa.userService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for UserService focusing on the findUserByEmail method.
 * 
 * This test class validates the email-based user lookup functionality
 * at the service layer, ensuring proper repository interactions and
 * exception handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService findUserByEmail tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private static final String TEST_EMAIL = "test@example.com";
    private static final UUID TEST_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(TEST_UUID);
        testUser.setEmail(TEST_EMAIL);
        testUser.setAlias("Test User");
        testUser.setAvatarUrl("https://example.com/avatar.jpg");
        testUser.setEmailConfirmed(true);
    }

    @Test
    @DisplayName("Should successfully find user by email when user exists")
    void findUserByEmail_WhenUserExists_ShouldReturnUserResponseDTO() {
        // Given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // When
        UserResponseDTO result = userService.findUserByEmail(TEST_EMAIL);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(result.getAlias()).isEqualTo("Test User");
        assertThat(result.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");

        verify(userRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist by email")
    void findUserByEmail_WhenUserDoesNotExist_ShouldThrowUserNotFoundException() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findUserByEmail(nonExistentEmail))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with email: " + nonExistentEmail);

        verify(userRepository).findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("Should handle null email parameter gracefully")
    void findUserByEmail_WithNullEmail_ShouldCallRepositoryWithNull() {
        // Given
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findUserByEmail(null))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with email: null");

        verify(userRepository).findByEmail(null);
    }

    @Test
    @DisplayName("Should handle empty email string parameter")
    void findUserByEmail_WithEmptyEmail_ShouldCallRepositoryWithEmptyString() {
        // Given
        String emptyEmail = "";
        when(userRepository.findByEmail(emptyEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findUserByEmail(emptyEmail))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with email: ");

        verify(userRepository).findByEmail(emptyEmail);
    }

    @Test
    @DisplayName("Should preserve email case sensitivity in repository call")
    void findUserByEmail_WithMixedCaseEmail_ShouldPreserveCaseInRepositoryCall() {
        // Given
        String mixedCaseEmail = "Test.User@Example.COM";
        User mixedCaseUser = new User();
        mixedCaseUser.setId(TEST_UUID);
        mixedCaseUser.setEmail(mixedCaseEmail);
        mixedCaseUser.setAlias("Mixed Case User");

        when(userRepository.findByEmail(mixedCaseEmail)).thenReturn(Optional.of(mixedCaseUser));

        // When
        UserResponseDTO result = userService.findUserByEmail(mixedCaseEmail);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(mixedCaseEmail);
        verify(userRepository).findByEmail(mixedCaseEmail);
    }

    @Test
    @DisplayName("Should handle special characters in email correctly")
    void findUserByEmail_WithSpecialCharactersInEmail_ShouldWorkCorrectly() {
        // Given
        String specialEmail = "user+test.tag@example-domain.co.uk";
        User specialUser = new User();
        specialUser.setId(TEST_UUID);
        specialUser.setEmail(specialEmail);
        specialUser.setAlias("Special User");

        when(userRepository.findByEmail(specialEmail)).thenReturn(Optional.of(specialUser));

        // When
        UserResponseDTO result = userService.findUserByEmail(specialEmail);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(specialEmail);
        verify(userRepository).findByEmail(specialEmail);
    }

    @Test
    @DisplayName("Should correctly map all user fields from entity to DTO")
    void findUserByEmail_ShouldMapAllFieldsCorrectly() {
        // Given
        User userWithAllFields = new User();
        userWithAllFields.setId(TEST_UUID);
        userWithAllFields.setEmail("complete@example.com");
        userWithAllFields.setAlias("Complete User");
        userWithAllFields.setAvatarUrl("https://example.com/complete-avatar.jpg");
        userWithAllFields.setEmailConfirmed(false);

        when(userRepository.findByEmail("complete@example.com")).thenReturn(Optional.of(userWithAllFields));

        // When
        UserResponseDTO result = userService.findUserByEmail("complete@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getEmail()).isEqualTo("complete@example.com");
        assertThat(result.getAlias()).isEqualTo("Complete User");
        assertThat(result.getAvatarUrl()).isEqualTo("https://example.com/complete-avatar.jpg");

        verify(userRepository).findByEmail("complete@example.com");
    }

    @Test
    @DisplayName("Should handle whitespace in email parameter")
    void findUserByEmail_WithWhitespaceInEmail_ShouldPreserveWhitespace() {
        // Given
        String emailWithWhitespace = " user@example.com ";
        when(userRepository.findByEmail(emailWithWhitespace)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findUserByEmail(emailWithWhitespace))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with email: " + emailWithWhitespace);

        verify(userRepository).findByEmail(emailWithWhitespace);
    }
}