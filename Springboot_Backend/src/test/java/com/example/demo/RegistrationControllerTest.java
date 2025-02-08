package com.example.demo;

import com.example.demo.controller.RegistrationController;
import com.example.demo.model.Location;
import com.example.demo.service.RegistrationService;
import com.example.demo.utils.registration.RegistrationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    private RegistrationRequest validRegistrationRequest;
    private Location location;

    @BeforeEach
    void setUp() {
        // Create a valid registration request for testing
        location = new Location();
        location.setLatitude(40.7128);
        location.setLongitude(-74.0060);

        validRegistrationRequest = new RegistrationRequest(
                "John",
                "Doe",
                "jorbon.doe@example.com",
                "password123",
                "1234567890",
                location,
                Arrays.asList("Wheat", "Corn")
        );
    }

    @Test
    @DisplayName("Register User - Successful Registration")
    void testRegisterUser_Successful() throws Exception {
        // Mock the service to return a token
        when(registrationService.register(any(RegistrationRequest.class)))
                .thenReturn("test-confirmation-token");

        mockMvc.perform(post("/api/v1/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("test-confirmation-token"));
    }
}
