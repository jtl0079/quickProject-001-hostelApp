package com.example.hostelmanagement.report.usecase

import com.example.hostelmanagement.report.domain.repository.HostelReportRepository
import com.example.hostelmanagement.report.infrastructure.repository.HostelReportRepositoryImpl
import javax.inject.Inject

class GetReportUseCase (
    private val repository: HostelReportRepository
) {

    operator fun invoke(
        branchId: String,
        frequency: String,   // year / month
        target: String,
        startYear: Int,
        startMonth: Int,
        endYear: Int,
        endMonth: Int
    ) =
        repository.observeReportsByBranch(
            branchId = branchId,
            frequency = frequency,
            target = target,
            startYear = startYear,
            startMonth = startMonth +1, // because receive 0-11
            endYear = endYear,
            endMonth = endMonth +1

        )


}
