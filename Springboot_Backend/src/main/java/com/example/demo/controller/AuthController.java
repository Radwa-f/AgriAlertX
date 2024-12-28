package com.example.demo.controller;

import com.example.demo.utils.authentication.AuthRequest;
import com.example.demo.utils.authentication.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest authenticationRequest) {
        try {
            // Authenticate the user and generate a token
            String token = authService.authenticate(authenticationRequest);

            // Return the JWT token in response
            return ResponseEntity.ok(token);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>("Authentication failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
}

