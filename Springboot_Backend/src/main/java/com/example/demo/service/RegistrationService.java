package com.example.demo.service;

import com.example.demo.model.AppUser;
import com.example.demo.model.AppUserRole;
import com.example.demo.model.Location;
import com.example.demo.repository.LocationRepository;
import com.example.demo.utils.registration.EmailValidator;
import com.example.demo.utils.registration.RegistrationRequest;
import com.example.demo.utils.email.EmailSender;
import com.example.demo.utils.registration.token.ConfirmationToken;
import com.example.demo.utils.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final AppUserService appUserService;
    private final EmailValidator emailValidator;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;
    private final LocationRepository locationRepository ;

    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());

        if (!isValidEmail) {
            throw new IllegalStateException("email not valid");
        }
        Location location = new Location();
        location.setLatitude(request.getLocation().getLatitude());
        location.setLongitude(request.getLocation().getLongitude());

// Save the location if needed (if not already managed by cascading)
        location = locationRepository.save(location);

        AppUser user = new AppUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhoneNumber(),
                null,
                null,
                AppUserRole.USER,
                location, // Set the location here
                request.getCrops()
        );

        String token = appUserService.signUpUser(user);


        String link = "http://localhost:8087/api/v1/registration/confirm?token=" + token;
        emailSender.send(
                request.getEmail(),
                buildEmail(request.getFirstName(), link)
        );

        return token;
    }


    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() -> new IllegalStateException("token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        appUserService.enableAppUser(
                confirmationToken.getAppUser().getEmail());
        return "confirmed";
    }

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "  <tbody><tr>\n" +
                "    <td width=\"100%\" height=\"53\" bgcolor=\"#d4edda\">\n" + // Light green background
                "      <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "        <tbody><tr>\n" +
                "          <td width=\"70\" bgcolor=\"#d4edda\" valign=\"middle\">\n" +
                "            <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "              <tbody><tr>\n" +
                "                <td style=\"padding-left:10px\"></td>\n" +
                "                <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                  <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#155724;text-decoration:none;vertical-align:top;display:inline-block\">Welcome to AgriAlert!</span>\n" +
                "                </td>\n" +
                "              </tr>\n" +
                "            </tbody></table>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "      </tbody></table>\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "</tbody></table>\n" +
                "\n" +
                "<table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "  <tbody><tr>\n" +
                "    <td style=\"background-color:#e2f0fb;padding:20px;border-radius:8px;\"> <!-- Light blue background -->\n" +
                "      <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p>\n" +
                "      <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Thank you for registering with AgriAlert. Please click the link below to activate your account:</p>\n" +
                "      <blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px;color:#0b0c0c\">\n" +
                "        <a href=\"" + link + "\" style=\"color:#1D70B8;text-decoration:none;font-weight:bold\">Activate Now</a>\n" +
                "      </blockquote>\n" +
                "      <p style=\"Margin:0;font-size:19px;line-height:25px;color:#0b0c0c\">This link will expire in 15 minutes.</p>\n" +
                "      <p style=\"Margin:0;font-size:19px;line-height:25px;color:#0b0c0c\">See you soon!</p>\n" +
                "    </td>\n" +
                "  </tr>\n" +
                "</tbody></table>\n" +
                "</div>";
    }

}
