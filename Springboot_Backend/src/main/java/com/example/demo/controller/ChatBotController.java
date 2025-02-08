package com.example.demo.controller;

import com.example.demo.model.ChatRequest;
import com.example.demo.model.ChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/chatbot")
public class ChatBotController {

    private static final String FLASK_CHATBOT_URL = "http://localhost:5000/chat";

    @PostMapping
    public ResponseEntity<ChatResponse> forwardChatRequest(@RequestBody ChatRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ChatResponse> response = restTemplate.postForEntity(FLASK_CHATBOT_URL, request, ChatResponse.class);
        return ResponseEntity.ok(response.getBody());
    }
}
