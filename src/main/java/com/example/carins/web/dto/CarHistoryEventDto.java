package com.example.carins.web.dto;

import java.time.LocalDate;

public record CarHistoryEventDto(String eventType, LocalDate date, String description, Long relatedId) {}