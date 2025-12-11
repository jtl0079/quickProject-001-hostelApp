package com.example.hostelmanagement.report.infrastructure.dto.mapper

import com.example.hostelmanagement.report.infrastructure.dto.model.TimeEntryDto
import com.myorg.kotlintools.time.domain.model.instant.InstantlyEntry
import java.time.Instant

fun TimeEntryDto.toDomain(): InstantlyEntry<String, Double> {
    return InstantlyEntry(
        key = key,
        timestamp = Instant.ofEpochMilli(timestamp),
        value = value
    )
}

fun InstantlyEntry<String, Double>.toDto(): TimeEntryDto {
    return TimeEntryDto(
        key = key,
        timestamp = timestamp.toEpochMilli(),
        value = value
    )
}
