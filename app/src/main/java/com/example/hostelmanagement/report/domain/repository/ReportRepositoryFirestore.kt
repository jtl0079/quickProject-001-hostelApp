package com.example.hostelmanagement.report.domain.repository

import com.example.hostelmanagement.report.domain.model.HostelReport

interface HostelReportRepositoryFirestore{



    suspend fun getReportsByBranch(branchId: String): List<HostelReport>


}