package com.example.demo.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NewsResponseDto {
    private List<NewsItemDto> news;
}