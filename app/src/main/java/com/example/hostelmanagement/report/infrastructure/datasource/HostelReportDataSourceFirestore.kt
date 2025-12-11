package com.example.hostelmanagement.report.infrastructure.datasource

import com.example.hostelmanagement.report.infrastructure.dto.model.HostelReportDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class HostelReportDataSourceFirestore(
    private val dbFirestore: FirebaseFirestore
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

    suspend fun uploadReport(branchId: String, dto: HostelReportDto) {
        collection.document(dto.reportId)
            .set(dto)
            .await()
    }

    suspend fun save(dto: HostelReportDto) {
        collection.document(dto.reportId).set(dto).await()
    }
}
