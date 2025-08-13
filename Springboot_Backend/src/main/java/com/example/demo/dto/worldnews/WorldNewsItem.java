package com.example.demo.dto.worldnews;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorldNewsItem {
    private String title;
    private String text;
    private String url;
    private String image;
}
