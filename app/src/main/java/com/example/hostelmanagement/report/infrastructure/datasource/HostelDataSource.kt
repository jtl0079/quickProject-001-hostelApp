package com.example.hostelmanagement.report.infrastructure.datasource

import com.example.hostelmanagement.HostelUiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object HostelDataSource {

    suspend fun loadAllHostelsInBranch(): List<HostelUiState> {

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser?.uid ?: return emptyList()

        val snapshot = db.collection("Branch")
            .document(uid)
            .collection("Hostel")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            HostelUiState(
                hostelId = doc.getString("hostelId") ?: doc.id,
                hostelPicture = doc.getString("hostelPicture") ?: "",
                hostelName = doc.getString("hostelName") ?: "",
                hostelAddress = doc.getString("hostelAddress") ?: "",
                hostelCity = doc.getString("hostelCity") ?: "",
                hostelState = doc.getString("hostelState") ?: "",
                hostelPostalCode = doc.getString("hostelPostalCode") ?: ""
            )
        }
    }

    suspend fun findHostelByRoomId(
        roomId: String
    ): HostelUiState? {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        val db = FirebaseFirestore.getInstance()

        // 1️⃣ 找到 room 所在 hostelId
        val hostelsSnap = db.collection("Branch")
            .document(uid)
            .collection("Hostel")
            .get()
            .await()

        for (hostelDoc in hostelsSnap.documents) {

            val roomsSnap = hostelDoc.reference
                .collection("rooms")
                .whereEqualTo("roomId", roomId)
                .get()
                .await()

            if (!roomsSnap.isEmpty) {
                // 2️⃣ 找到对应 hostel，直接转对象
                return HostelUiState(
                    hostelId = hostelDoc.getString("hostelId") ?: hostelDoc.id,
                    hostelName = hostelDoc.getString("hostelName") ?: "",
                    hostelPicture = hostelDoc.getString("hostelPicture") ?: "",
                    hostelAddress = hostelDoc.getString("hostelAddress") ?: "",
                    hostelCity = hostelDoc.getString("hostelCity") ?: "",
                    hostelState = hostelDoc.getString("hostelState") ?: "",
                    hostelPostalCode = hostelDoc.getString("hostelPostalCode") ?: ""
                )
            }
        }

        return null
    }

}