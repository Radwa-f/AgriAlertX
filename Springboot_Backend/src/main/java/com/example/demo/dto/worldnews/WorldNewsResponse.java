package com.example.demo.dto.worldnews;

import lombok.*;
import java.util.List;

@Getter @Setter
public class WorldNewsResponse {
    private List<WorldNewsItem> news;
}

