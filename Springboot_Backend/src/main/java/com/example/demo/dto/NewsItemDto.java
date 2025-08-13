package com.example.demo.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NewsItemDto {
    private String title;
    private String text;
    private String url;
    private String image;
}