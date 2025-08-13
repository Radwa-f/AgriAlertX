package com.example.demo.dto.image;


import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImageResponse {
    private String url;  // single URL your app will load
}
