package com.example.hostelmanagement.report.infrastructure.dto.mapper

import android.util.Log
import com.example.hostelmanagement.report.domain.model.HostelReport
import com.example.hostelmanagement.report.infrastructure.dto.model.HostelReportDto
import com.myorg.kotlintools.android.ui.chart.PieChartData
import com.myorg.kotlintools.time.domain.model.alltime.KeyAllTimeMap
import java.time.Instant
import kotlin.collections.component1
import kotlin.collections.component2

fun HostelReportDto.toDomain(): HostelReport {
    return HostelReport(
        reportId = reportId,
        reportName = reportName,
        periodStartDate = Instant.ofEpochMilli(periodStartDate),
        periodEndDate = Instant.ofEpochMilli(periodEndDate),
        frequency = frequency,
        target = target,
        data = data.toDomain(),
        branchId = branchId
    )
}

fun HostelReport.toDto(): HostelReportDto {
    return HostelReportDto(
        reportId = reportId,
        reportName = reportName,
        periodStartDate = periodStartDate.toEpochMilli(),
        periodEndDate = periodEndDate.toEpochMilli(),
        frequency = frequency,
        target = target,
        data = data.toDto(),
        branchId = branchId
    )
}

fun <TKey> KeyAllTimeMap<TKey, Double>.toPieChartDataList(
    frequency: String = "month",
    keyFormatter: (TKey) -> String = { it.toString() }
): List<PieChartData> {

    Log.d(
        "MAPPER_PIE",
        "keyTimeMap = ${keyTimeMap.size}]"
    )
    return keyTimeMap.map { (key, group) ->

        val totalValue = when (frequency.lowercase().trim()) {
            "year" -> group.yearlyMap.timeMap.values.sum()
            else -> group.monthlyMap.timeMap.values.sum()
        }

        Log.d(
            "PIE_DEBUG",
            "frequency=[$frequency], normalized=[${frequency.lowercase()}]"
        )

        PieChartData(
            name = keyFormatter(key),
            value = totalValue
        )


    }
}