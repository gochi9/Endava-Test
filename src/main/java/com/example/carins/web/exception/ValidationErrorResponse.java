package com.example.carins.web.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationErrorResponse(int status, String error, String message, LocalDateTime timestamp, Map<String, String> validationErrors) {}