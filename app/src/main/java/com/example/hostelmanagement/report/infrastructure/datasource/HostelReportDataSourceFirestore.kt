package com.example.hostelmanagement.report.infrastructure.datasource

import com.example.hostelmanagement.report.infrastructure.dto.model.HostelReportDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class HostelReportDataSourceFirestore(
    private val dbFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val collection = dbFirestore.collection("hostel_report")

    suspend fun getByBranch(branchId: String): List<HostelReportDto> =
        collection
            .whereEqualTo("branchId", branchId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(HostelReportDto::class.java) }

    fun observeReportsByBranch(branchId: String): Flow<List<HostelReportDto>> = callbackFlow {
        val listener = collection.document(branchId)
            .collection("report")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val list = snap!!.documents.mapNotNull { it.toObject(HostelReportDto::class.java) }
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    fun observeReports(
        branchId: String,
        frequency: String,
        startYear: Int,
        startMonth: Int,
        endYear: Int,
        endMonth: Int
    ): Flow<HostelReportDto?> = callbackFlow {

        val startInstant = LocalDate.of(startYear, startMonth, 1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val endInstant = YearMonth.of(endYear, endMonth)
            .atEndOfMonth()
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val registration = dbFirestore
            .collection("hostel_report")
            .whereEqualTo("branchId", branchId)
            .whereEqualTo("frequency", frequency)
            .whereGreaterThanOrEqualTo("periodStartDate", startInstant)
            .whereLessThanOrEqualTo("periodStartDate", endInstant)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val report = snapshot
                    ?.documents
                    ?.firstOrNull()
                    ?.toObject(HostelReportDto::class.java)

                trySend(report)
            }

        awaitClose { registration.remove() }
    }


    suspend fun uploadReport(branchId: String, dto: HostelReportDto) {
        collection.document(dto.reportId)
            .set(dto)
            .await()
    }



    suspend fun deleteReport(reportId: String) {
        collection.document(reportId).delete().await()
    }
}
