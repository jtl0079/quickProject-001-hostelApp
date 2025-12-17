package com.example.hostelmanagement.report.infrastructure.datasource

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Branch(
    val branchId: String = "",
    val branchName: String = "",
    val branchEmail: String = "",
    val branchPhoneNumber: String = "",
    val branchPassword: String = "",
    val branchSecretWord: String = ""
)


object BranchDataSource {

    suspend fun getCurrentBranch(): Branch? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return null

        val snap = FirebaseFirestore.getInstance()
            .collection("Branch")
            .document(uid)
            .get()
            .await()

        if (!snap.exists()) return null
        return snap.toObject(Branch::class.java)
    }


    suspend fun getBranchIdByUid(): String? {

        return getCurrentBranch()?.branchName
    }

}