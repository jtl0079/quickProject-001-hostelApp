package com.example.hostelmanagement.maintenance

import android.util.Log
import coil.util.CoilUtils.result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java

class MaintenanceRepository(
    private val data: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {

    fun saveMaintenanceToFirebase(
        uId: String,
        hostelId: String,
        roomId: String,
        maintenance: Maintenance,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ){
        data.collection("Branch")
            .document(uId)
            .collection("Hostel")
            .document(hostelId)
            .collection("rooms")
            .document(roomId)
            .collection("maintenances")
            .document(maintenance.maintenanceId)
            .set(maintenance)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun readMaintenanceFromFirebase(
        uId: String,
        hostelId: String,
        roomId: String,
        maintenanceId : String,
        onResult: (Maintenance?) -> Unit,
        onError: (Exception) -> Unit
    ){
        data.collection("Branch")
            .document(uId)
            .collection("Hostel")
            .document(hostelId)
            .collection("rooms")
            .document(roomId)
            .collection("maintenances")
            .document(maintenanceId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val maintenance = document.toObject(Maintenance::class.java)
                    onResult(maintenance)
                } else {
                    Log.d("Firestore", "No such document")
                    onResult(null)
                }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun readAllMaintenancesFromFirebase(
        uId: String,
        hostelId: String,
        roomId: String,
        onSuccess: (List<Maintenance>) -> Unit,
        onError:(Exception) -> Unit
    ){
        data.collection("Branch")
            .document(uId)
            .collection("Hostel")
            .document(hostelId)
            .collection("rooms")
            .document(roomId)
            .collection("maintenances")
            .whereEqualTo("roomId", roomId)
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Maintenance::class.java)
                onSuccess(list)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun updateMaintenanceInFirebase(
        uId: String,
        hostelId: String,
        roomId: String,
        maintenanceId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        data.collection("Branch")
            .document(uId)
            .collection("Hostel")
            .document(hostelId)
            .collection("rooms")
            .document(roomId)
            .collection("maintenances")
            .document(maintenanceId)
            .update(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun updateRoomStatus(
        uId: String,
        hostelId: String,
        roomId: String
    ) {
        val maintenanceCollection = data.collection("Branch")//find out all the maintenance list (find the path)
            .document(uId)
            .collection("Hostel")
            .document(hostelId)
            .collection("rooms")
            .document(roomId)
            .collection("maintenances")


        maintenanceCollection.get()//take out all the maintenance list from firebase
            .addOnSuccessListener { results ->
                val list = results.toObjects(Maintenance::class.java)


                val hasActive = list.any {//find out the request that is in progress
                    it.status == MaintenanceStatus.IN_PROGRESS
                }

                val newStatus = if (hasActive) "Unavailable" else "Available"//if got the hasActive will be true then change the status to unavailable


                data.collection("Branch")//update the room status firebase
                    .document(uId)
                    .collection("Hostel")
                    .document(hostelId)
                    .collection("rooms")
                    .document(roomId)
                    .update("roomStatus", newStatus)
                    .addOnSuccessListener {
                        Log.d("RoomViewModel", "Room status updated successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("RoomViewModel", "Error updating room status: $e")
                    }

            }
            .addOnFailureListener{ e ->
                Log.e("RoomViewModel", "Error getting maintenance list: $e")
            }
    }

    fun deleteMaintenanceFromFirebase(
        uId: String,
        hostelId: String,
        roomId: String,
        maintenanceId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        data.collection("Branch")
            .document(uId)
            .collection("Hostel")
            .document(hostelId)
            .collection("rooms")
            .document(roomId)
            .collection("maintenances")
            .document(maintenanceId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}