package com.example.hostelmanagement.report.domain.repository

import com.example.hostelmanagement.report.domain.model.HostelReport
import kotlinx.coroutines.flow.Flow

interface HostelReportRepository {
    fun observeReportsByBranch(
        branchId: String,
        frequency: String,   // year / month
        target: String,
        startYear: Int,
        startMonth: Int,
        endYear: Int,
        endMonth: Int
    ) : Flow<HostelReport?>

    suspend fun saveReport(report: HostelReport)
}
