package com.example.hostelmanagement.report.infrastructure.repository

import com.example.hostelmanagement.booking.App
import com.example.hostelmanagement.report.domain.model.HostelReport
import com.example.hostelmanagement.report.infrastructure.datasource.HostelReportDataSourceFirestore
import com.example.hostelmanagement.report.infrastructure.dto.mapper.toDomain
import com.example.hostelmanagement.report.infrastructure.dto.mapper.toDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// TODO //
//val bookingRepo = (application as App).bookingRepository
//val qwe = bookingRepo.getAllBooking()


class ReportRepositoryFirestoreImpl(
    private val dataSource: HostelReportDataSourceFirestore
) {
    suspend fun getReportsByBranch(branchId: String): List<HostelReport> =
        dataSource.getByBranch(branchId).map { it.toDomain() }

    fun observeReportsByBranch(branchId: String): Flow<List<HostelReport>> {
        return dataSource.observeReportsByBranch(branchId)
            .map { list -> list.map { it.toDomain() } }
    }

    suspend fun saveReport(report: HostelReport) {
        val dto = report.toDto()
        dataSource.uploadReport(report.branchId, dto)
    }
}
