package com.example.hostelmanagement

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RoomViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val storageRepo: StorageRepository = StorageRepository()
) : ViewModel() {
    private val _roomsState = MutableStateFlow<List<RoomItem>>(emptyList())
    val roomsState: StateFlow<List<RoomItem>> = _roomsState.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private var roomsListener: ListenerRegistration? = null
    private var currentObservedHostelId: String? = null

    suspend fun addRoomWithDetails(
        hostelId: String,
        selectedImageUri: Uri?,
        roomType: String,
        roomPrice: Double,
        roomCapacity: Int,
        roomDescription: String,
        hostelName: String = ""
    ): String = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid
            ?: throw kotlin.IllegalStateException("User not logged in")

        val safeHostelId = hostelId
            .trim()
            .trim('/')
            .split('/')
            .lastOrNull()
            ?: throw kotlin.IllegalStateException("Invalid hostelId: '$hostelId'")

        try {
            var safeHostelName = hostelName.ifBlank { "" }
            if (safeHostelName.isBlank()) {
                val hostelDoc = firestore.collection("Branch")
                    .document(uid)
                    .collection("Hostel")
                    .document(safeHostelId)
                    .get()
                    .await()
                safeHostelName = hostelDoc.getString("hostelName") ?: safeHostelId
            }
            val prefix = RoomIdUtils.createPrefix(safeHostelName)

            val roomsCollection = firestore.collection("Branch")
                .document(uid)
                .collection("Hostel")
                .document(safeHostelId)
                .collection("rooms")

            val snapshots = roomsCollection.get().await()

            val maxNum = snapshots.documents
                .mapNotNull { doc ->
                    val rid = doc.getString("roomId") ?: doc.id
                    if (rid.startsWith(prefix, ignoreCase = true)) {
                        RoomIdUtils.extractNumber(rid)
                    } else null
                }.maxOrNull() ?: 0

            val nextNumber = maxNum + 1
            val roomId = prefix + String.format("%03d", nextNumber)

            val photoUrl: String? = selectedImageUri?.let { uri ->
                storageRepo.uploadRoomImage(uid, safeHostelId, uri)
            }

            val roomMap = hashMapOf<String, Any>(
                "roomId" to roomId,
                "roomType" to roomType,
                "roomPrice" to roomPrice,
                "roomCapacity" to roomCapacity,
                "roomDescription" to roomDescription,
                "createdAt" to Timestamp.now(),
                "roomStatus" to "Available",//
                "hostelId" to safeHostelId//
            )
            photoUrl?.let { roomMap.put("photoUrl", it) }

            roomsCollection.document(roomId).set(roomMap).await()

            withContext(Dispatchers.Main) {
                val newItem = RoomItem(
                    roomId = roomId,
                    photoUrl = photoUrl,
                    roomType = roomType,
                    roomCapacity = roomCapacity,
                    roomPrice = roomPrice,
                    roomDescription = roomDescription,
                    createdAt = roomMap["createdAt"] as Timestamp,
                    roomStatus = "Available",//
                    hostelId = safeHostelId//
                )
                val updatedList = _roomsState.value.toMutableList()
                updatedList.add(newItem)
                _roomsState.value = RoomIdUtils.sortRooms(updatedList)
            }
            roomId
        } catch (e: Exception) {
            throw e
        }
    }


    fun observeRooms(hostelId: String) {
        val uid = auth.currentUser?.uid ?: return
        val safeHostelId = hostelId
            .trim()
            .trim('/')
            .split('/')
            .lastOrNull() ?: return

        if (roomsListener != null && currentObservedHostelId == safeHostelId) {
            return
        }
        roomsListener?.remove()
        roomsListener = null
        currentObservedHostelId = safeHostelId

        val coll = firestore.collection("Branch")
            .document(uid)
            .collection("Hostel")
            .document(safeHostelId)
            .collection("rooms")

        _loading.value = true
        _error.value = null

        roomsListener = coll.addSnapshotListener { snaps, e ->
            if (e != null) {
                _error.value = e.message
                _loading.value = false
                return@addSnapshotListener
            }
            if (snaps != null) {
                val list = snaps.documents.map { d ->
                    RoomItem(
                        roomId = d.getString("roomId") ?: d.id,
                        photoUrl = d.getString("photoUrl"),
                        roomType = d.getString("roomType"),
                        roomCapacity = d.getLong("roomCapacity")?.toInt(),
                        roomPrice = d.getDouble("roomPrice"),
                        roomDescription = d.getString("roomDescription"),
                        createdAt = d.getTimestamp("createdAt")
                    )
                }.sortedBy { it.roomId }
                _roomsState.value = list
            } else {
                _roomsState.value = emptyList()
            }
            _loading.value = false
        }
    }

    suspend fun loadRoomsOnce(hostelId: String): List<RoomItem> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: throw kotlin.IllegalStateException("User not logged in")
        val safeHostelId = hostelId
            .trim()
            .trim('/')
            .split('/')
            .lastOrNull() ?: throw kotlin.IllegalStateException("Invalid hostelId")

        val snaps = firestore.collection("Branch")
            .document(uid)
            .collection("Hostel")
            .document(safeHostelId)
            .collection("rooms")
            .get()
            .await()

        val list = snaps.documents.map { d ->
            RoomItem(
                roomId = d.getString("roomId") ?: d.id,//
                photoUrl = d.getString("photoUrl"),
                roomType = d.getString("roomType"),
                roomCapacity = d.getLong("roomCapacity")?.toInt(),
                roomPrice = d.getDouble("roomPrice"),
                roomDescription = d.getString("roomDescription"),
                createdAt = d.getTimestamp("createdAt")
            )
        }.sortedBy { it.roomId }

        withContext(Dispatchers.Main) {
            _roomsState.value = list
        }
        list
    }

    fun updateRoom(
        hostelId: String,
        roomId: String,
        roomPrice: Double,
        roomDescription: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onError("User not logged in")

        val safeHostelId = hostelId.trim().trim('/').split('/').lastOrNull()
            ?: return onError("Invalid hostelId: '$hostelId'")

        viewModelScope.launch {
            try {
                val docRef = firestore.collection("Branch")
                    .document(uid)
                    .collection("Hostel")
                    .document(safeHostelId)
                    .collection("rooms")
                    .document(roomId)
                docRef.update(
                    mapOf(
                        "roomPrice" to roomPrice,
                        "roomDescription" to roomDescription
                    )
                ).await()

                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun deleteRoom(
        hostelId: String,
        roomId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onError("User not logged in")

        val safeHostelId = hostelId.trim().trim('/').split('/').lastOrNull()
            ?: return onError("Invalid hostelId: '$hostelId'")

        viewModelScope.launch {
            try {
                val docRef = firestore.collection("Branch")
                    .document(uid)
                    .collection("Hostel")
                    .document(safeHostelId)
                    .collection("rooms")
                    .document(roomId)
                docRef.delete().await()

                val current = _roomsState.value.toMutableList()
                val index = current.indexOfFirst { it.roomId == roomId }
                if (index != -1) {
                    current.removeAt(index)
                    _roomsState.value = current
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Delete failed")
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        roomsListener?.remove()
        roomsListener = null
        currentObservedHostelId = null
    }
}