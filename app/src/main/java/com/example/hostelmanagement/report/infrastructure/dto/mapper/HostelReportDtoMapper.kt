package com.example.hostelmanagement.report.infrastructure.dto.mapper

import com.example.hostelmanagement.report.domain.model.HostelReport
import com.example.hostelmanagement.report.infrastructure.dto.model.HostelReportDto
import java.time.Instant

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
