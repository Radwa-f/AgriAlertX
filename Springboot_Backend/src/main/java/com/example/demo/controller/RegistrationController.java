package com.example.demo.controller;

import com.example.demo.utils.registration.RegistrationRequest;
import com.example.demo.service.RegistrationService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/registration")
@AllArgsConstructor
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    private final RegistrationService registrationService;

    @PostMapping(consumes = "application/json")
    public String register(@RequestBody RegistrationRequest request, @RequestHeader HttpHeaders headers) {
        // You can now access the raw request body and the headers
        logger.info("Received body: {}" , request);
        logger.info("Content-Type: {}" , headers.getContentType());
        return registrationService.register(request);
    }

    @GetMapping(path = "confirm")
    public String confirm(@RequestParam("token") String token) {
        return registrationService.confirmToken(token);
    }

}
