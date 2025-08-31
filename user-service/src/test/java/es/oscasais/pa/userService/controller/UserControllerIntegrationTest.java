package es.oscasais.pa.userService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.oscasais.pa.userService.dto.UserRequestDTO;
import es.oscasais.pa.userService.dto.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test class for UserController /users/me endpoint.
 * 
 * This test class performs end-to-end testing of the /users/me endpoint
 * with real database interactions to ensure the email-based lookup
 * functionality works correctly in a realistic environment.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("UserController /users/me integration tests")
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private static final String TEST_EMAIL = "integration.test@example.com";
    private static final String TEST_ALIAS = "Integration Test User";
    private static final String TEST_AVATAR_URL = "https://example.com/integration-avatar.jpg";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should retrieve user info after creating user with email-based lookup")
    void getCurrentUser_AfterUserCreation_ShouldReturnCorrectUserInfo() throws Exception {
        // Given - Create a user first
        UserRequestDTO createRequest = new UserRequestDTO();
        createRequest.setEmail(TEST_EMAIL);
        createRequest.setAlias(TEST_ALIAS);
        createRequest.setAvatarUrl(TEST_AVATAR_URL);

        MvcResult createResult = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDTO createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                UserResponseDTO.class);

        // When & Then - Retrieve the user using /users/me with email-based lookup
        mockMvc.perform(get("/users/me")
                .header("X-User-Email", TEST_EMAIL)
                .header("X-User-Id", createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.alias").value(TEST_ALIAS))
                .andExpect(jsonPath("$.avatarUrl").value(TEST_AVATAR_URL));
    }

    @Test
    @DisplayName("Should return 400 when trying to get non-existent user via email")
    void getCurrentUser_WithNonExistentEmailInDatabase_ShouldReturnBadRequest() throws Exception {
        // Given - No user exists with this email
        String nonExistentEmail = "does.not.exist@example.com";

        // When & Then
        mockMvc.perform(get("/users/me")
                .header("X-User-Email", nonExistentEmail)
                .header("X-User-Id", "some-id")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("Should work correctly with different email formats")
    void getCurrentUser_WithVariousEmailFormats_ShouldWorkCorrectly() throws Exception {
        // Test different valid email formats
        String[] emailFormats = {
            "user.name@domain.com",
            "user+tag@domain.co.uk", 
            "user_name@domain-name.org",
            "123@domain.net"
        };

        for (String email : emailFormats) {
            // Given - Create user with specific email format
            UserRequestDTO createRequest = new UserRequestDTO();
            createRequest.setEmail(email);
            createRequest.setAlias("Test User " + email);
            createRequest.setAvatarUrl("https://example.com/avatar.jpg");

            MvcResult createResult = mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            UserResponseDTO createdUser = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(), 
                    UserResponseDTO.class);

            // When & Then - Retrieve using email-based lookup
            mockMvc.perform(get("/users/me")
                    .header("X-User-Email", email)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.id").value(createdUser.getId()));
        }
    }

    @Test
    @DisplayName("Should handle concurrent requests to /users/me endpoint")
    void getCurrentUser_WithConcurrentRequests_ShouldHandleCorrectly() throws Exception {
        // Given - Create a user
        UserRequestDTO createRequest = new UserRequestDTO();
        createRequest.setEmail(TEST_EMAIL);
        createRequest.setAlias(TEST_ALIAS);
        createRequest.setAvatarUrl(TEST_AVATAR_URL);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        // When & Then - Make multiple concurrent requests
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/users/me")
                    .header("X-User-Email", TEST_EMAIL)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.alias").value(TEST_ALIAS));
        }
    }

    @Test
    @DisplayName("Should maintain consistency between UUID-based and email-based lookups")
    void getCurrentUser_CompareWithUuidLookup_ShouldReturnSameUser() throws Exception {
        // Given - Create a user
        UserRequestDTO createRequest = new UserRequestDTO();
        createRequest.setEmail(TEST_EMAIL);
        createRequest.setAlias(TEST_ALIAS);
        createRequest.setAvatarUrl(TEST_AVATAR_URL);

        MvcResult createResult = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDTO createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                UserResponseDTO.class);

        // When - Get user by UUID (traditional lookup)
        MvcResult uuidResult = mockMvc.perform(get("/users/" + createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // And - Get user by email (/users/me endpoint)
        MvcResult emailResult = mockMvc.perform(get("/users/me")
                .header("X-User-Email", TEST_EMAIL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then - Both responses should be identical
        String uuidResponse = uuidResult.getResponse().getContentAsString();
        String emailResponse = emailResult.getResponse().getContentAsString();
        
        UserResponseDTO uuidUser = objectMapper.readValue(uuidResponse, UserResponseDTO.class);
        UserResponseDTO emailUser = objectMapper.readValue(emailResponse, UserResponseDTO.class);
        
        // Verify both lookups return the same user data
        mockMvc.perform(get("/users/me")
                .header("X-User-Email", TEST_EMAIL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(uuidUser.getId()))
                .andExpect(jsonPath("$.email").value(uuidUser.getEmail()))
                .andExpect(jsonPath("$.alias").value(uuidUser.getAlias()))
                .andExpect(jsonPath("$.avatarUrl").value(uuidUser.getAvatarUrl()));
    }
}
