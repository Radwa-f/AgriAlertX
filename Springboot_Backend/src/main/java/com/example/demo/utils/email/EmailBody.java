package com.example.demo.utils.email;

import lombok.Builder;

@Builder
public record EmailBody(String to, String subject, String text) {

}
