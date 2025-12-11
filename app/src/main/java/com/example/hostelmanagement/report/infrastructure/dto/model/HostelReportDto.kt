package com.example.hostelmanagement.report.infrastructure.dto.model

data class HostelReportDto(
    val reportId: String = "",
    val reportName: String = "",
    val periodStartDate: Long = 0L,
    val periodEndDate: Long = 0L,
    val frequency: String = "",
    val target: String = "",
    val data: AllTimeDataBundleDto = AllTimeDataBundleDto(),
    val branchId: String = ""
)
