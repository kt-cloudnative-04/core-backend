package com.example.backend.module.message.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private String message;
    private LocalDateTime createdAt;
}
