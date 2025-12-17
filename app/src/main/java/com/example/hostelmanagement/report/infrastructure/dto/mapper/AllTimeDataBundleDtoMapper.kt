package com.example.hostelmanagement.report.infrastructure.dto.mapper

import com.example.hostelmanagement.report.infrastructure.dto.model.AllTimeDataBundleDto
import com.myorg.kotlintools.time.domain.model.alltime.AllTimeDataBundle
import com.myorg.kotlintools.time.domain.model.instant.InstantlyEntries
import com.myorg.kotlintools.valueOperatorOf


fun AllTimeDataBundleDto.toDomain(): AllTimeDataBundle<String, Double> {
    val domain = AllTimeDataBundle<String, Double>(
        timeEntries = InstantlyEntries(valueOperator = valueOperatorOf<Double>())
    )
    for (e in entries) {
        domain.timeEntries.entries.add(e.toDomain())
    }
    domain.syncMapByEntries()

    return domain
}

fun AllTimeDataBundle<String, Double>.toDto(): AllTimeDataBundleDto {
    return AllTimeDataBundleDto(
        entries = timeEntries.entries.map { it.toDto() }
    )
}
