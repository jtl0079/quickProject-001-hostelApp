package com.example.hostelmanagement.report.usecase

import com.example.hostelmanagement.report.domain.model.HostelReport
import com.example.hostelmanagement.report.domain.repository.HostelReportRepository


class SaveReportUseCase(
    private val repository: HostelReportRepository
) {

    suspend operator fun invoke(report: HostelReport) {
        repository.saveReport(report)
    }
}
