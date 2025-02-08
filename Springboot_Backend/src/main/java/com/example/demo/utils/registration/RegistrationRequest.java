package com.example.demo.utils.registration;

import com.example.demo.model.Location;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class RegistrationRequest {

    @NotEmpty(message = "First name cannot be empty")
    private final String firstName;

    @NotEmpty(message = "Last name cannot be empty")
    private final String lastName;

    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email cannot be empty")
    private final String email;

    @NotBlank(message = "Password cannot be blank")
    private final String password;

    @NotBlank(message = "Phone number cannot be blank")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private final String phoneNumber;

    private final Location location;  // Location of the user

    private final List<String> crops;  // List of crops associated with the user
}
