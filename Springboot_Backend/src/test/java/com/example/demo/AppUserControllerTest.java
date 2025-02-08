package com.example.demo;

import com.example.demo.model.AppUser;
import com.example.demo.model.AppUserRole;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.service.AppUserService;
import com.example.demo.utils.registration.token.ConfirmationTokenRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AppUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ConfirmationTokenRepository confirmationTokenRepository;

    @InjectMocks
    private AppUserService appUserService;

    @Test
    @WithMockUser(username="fattouhiradwa@gmail.com", roles={"USER"})
    void getProfile_ShouldReturnUserProfile() throws Exception {
        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="rd.fth2000@gmail.com", roles={"USER"})
    void updateProfile_ShouldUpdateUserDetails() throws Exception {
        AppUser updatedUser = new AppUser();
        updatedUser.setEmail("rd.fth2000@gmail.com");
        updatedUser.setFirstName("Janeee");
        updatedUser.setLastName("Doeee");
        updatedUser.setAppUserRole(AppUserRole.USER);

        when(appUserRepository.findByEmail("rd.fth2000@gmail.com")).thenReturn(Optional.of(new AppUser()));
        when(appUserRepository.save(any(AppUser.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updatedUser)))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser(username="john.doe@example.com", roles={"USER"})
    void deleteAccount_ShouldDeleteUserAccount() throws Exception {
        // Assume there's a user with the given email
        AppUser existingUser = new AppUser();
        existingUser.setEmail("ensajexams@gmail.com");

        when(appUserRepository.findByEmail("ensajexams@gmail.com")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(delete("/api/v1/user/delete"))
                .andExpect(status().isOk());

    }
}
