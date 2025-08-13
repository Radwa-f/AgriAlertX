package com.example.demo.service;

import com.example.demo.model.Location;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.LocationRepository;
import com.example.demo.model.AppUser;
import com.example.demo.utils.registration.token.ConfirmationToken;
import com.example.demo.utils.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private static final String USER_NOT_FOUND_MSG =
            "user with email %s not found";

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final LocationRepository locationRepository;
    //private final KafkaProducerService kafkaProducerService;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                String.format(USER_NOT_FOUND_MSG, email)));
    }

    public String signUpUser(AppUser appUser) {
        boolean userExists = appUserRepository
                .findByEmail(appUser.getEmail())
                .isPresent();

        if (userExists) {
            // TODO check of attributes are the same and
            // TODO if email not confirmed send confirmation email.

            throw new IllegalStateException("email already taken");
        }

        String encodedPassword = bCryptPasswordEncoder
                .encode(appUser.getPassword());

        appUser.setPassword(encodedPassword);

        appUserRepository.save(appUser);

        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser
        );

        confirmationTokenService.saveConfirmationToken(
                confirmationToken);

//        TODO: SEND EMAIL

        return token;
    }

    public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }

    // Example method to update the location after registration
    @Transactional
    public String updateLocation(String email, double latitude, double longitude) {
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Fetch or create a Location object
        Location location = appUser.getLocation() != null ? appUser.getLocation() : new Location();
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        // Save location if needed (if not cascading)
        locationRepository.save(location);

        // Associate with user and save user
        appUser.setLocation(location);
        appUserRepository.save(appUser);


        return "Location updated successfully!";
    }



}
