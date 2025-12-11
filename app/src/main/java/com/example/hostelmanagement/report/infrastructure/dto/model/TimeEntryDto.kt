package com.example.hostelmanagement.report.infrastructure.dto.model

data class TimeEntryDto(
    val key: String = "",
    val timestamp: Long = 0L,   // Instant → epochMilli
    val value: Double = 0.0
)
