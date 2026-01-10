package org.example.kortex.web;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        String message,
        String errorMessage,
        LocalDateTime errorTime
) {
}
