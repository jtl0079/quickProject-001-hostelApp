package com.example.hostelmanagement.booking.bookingHistory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanagement.RoomItem
import com.example.hostelmanagement.booking.booking.BookingRepository
import com.example.hostelmanagement.booking.booking.BookingStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import kotlin.collections.minusAssign
import kotlin.compareTo
import kotlin.text.get

class BookingHistoryViewModel(
    private val repository: BookingRepository,
    private val firestore: FirebaseFirestore= FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth =FirebaseAuth.getInstance(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingHistoryUiState())
    val uiState: StateFlow<BookingHistoryUiState> = _uiState
    private var roomsListener: ListenerRegistration?=null
    private var currentObservedHostelId:String?=null
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        repository.startSyncFromFirebase()
        observeRoomBookings()
    }

    override fun onCleared() {
        repository.stopSync()
        super.onCleared()
    }
    private fun observeRoomBookings() {

        viewModelScope.launch {
            repository.bookingsFlow.collect { list ->
                val complete = list.filter { it.bookingStatus == BookingStatus.Complete }
                val cancel = list.filter { it.bookingStatus == BookingStatus.Cancel }

                _uiState.value = _uiState.value.copy(
                    completeBooked = complete,
                    cancelBooked = cancel
                )
            }
        }
    }

    fun getRoom(): RoomItem? {
        return uiState.value.selectedRoom
    }
    private suspend fun checkRoomExistsAcrossHostelsSuspend(targetRoomId: String): Pair<Boolean, String?> {
        val uid = auth.currentUser?.uid ?: return Pair(false, null)

        val hostelsSnapshot = firestore.collection("Branch")
            .document(uid)
            .collection("Hostel")
            .get()
            .await()

        if (hostelsSnapshot.isEmpty) return Pair(false, null)

        for (hostelDoc in hostelsSnapshot.documents) {
            val roomsSnapshot = hostelDoc.reference.collection("rooms").get().await()
            for (r in roomsSnapshot.documents) {
                val roomId = r.getString("roomId") ?: r.id
                if (roomId == targetRoomId) {
                    return Pair(true, hostelDoc.id)
                }
            }
        }
        return Pair(false, null)
    }

    fun getHostelId( roomId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val (found, foundHostelId) = checkRoomExistsAcrossHostelsSuspend(roomId)
                _loading.value = false
                if (found) {
                    _uiState.value = _uiState.value.copy(
                        lastFoundHostelId = foundHostelId ?: ""
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        lastFoundHostelId = ""
                    )
                    _error.value = "Room not found"
                }
            } catch (ex: Exception) {
                _loading.value = false
                _error.value = ex.message
            }
        }
    }

    private suspend fun fetchRoomByHostelAndRoomId(hostelId: String, roomId: String): RoomItem? {
        val uid = auth.currentUser?.uid ?: return null
        val roomsSnap = firestore.collection("Branch")
            .document(uid)
            .collection("Hostel")
            .document(hostelId)
            .collection("rooms")
            .whereEqualTo("roomId", roomId)
            .get()
            .await()

        if (roomsSnap.isEmpty) return null

        val d = roomsSnap.documents.first()
        Log.d("RoomDebug", "fetchRoomByHostelAndRoomId: hostelId=$hostelId roomId=$roomId resultSize=${roomsSnap.size()}")
        Log.d("RoomDebug", "fetchRoomByHostelAndRoomId: roomId=${d.getDouble("roomPrice")} roomId=$roomId resultSize=${roomsSnap.size()}")

        return RoomItem(
            roomId = d.getString("roomId") ?: d.id,
            photoUrl = d.getString("photoUrl"),
            roomType = d.getString("roomType"),
            roomCapacity = d.getLong("roomCapacity")?.toInt(),
            roomPrice = d.getDouble("roomPrice"),
            roomDescription = d.getString("roomDescription"),
            createdAt = d.getTimestamp("createdAt"),
            roomStatus = d.getString("roomStatus") ?: "Available",
            hostelId = d.getString("hostelId")
        )
    }
    fun fetchRoomInfo(roomId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val (found, hostelId) = checkRoomExistsAcrossHostelsSuspend(roomId)
                if (!found || hostelId.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(selectedRoom = null)
                    _error.value = "Room not found"
                    return@launch
                }
                Log.d("RoomDebug", "Checking hostel: $hostelId")
                Log.d("RoomDebug", "Checking room: $roomId")

                val roomItem = fetchRoomByHostelAndRoomId(hostelId, roomId)
                if (roomItem != null) {
                    _uiState.value = _uiState.value.copy(selectedRoom = roomItem)
                    Log.d("RoomDebug", "Checking hostel: ${_uiState.value.selectedRoom}")

                } else {
                    _uiState.value = _uiState.value.copy(selectedRoom = null)
                    _error.value = "Room detail not found"
                }
            } catch (ex: Exception) {
                _error.value = ex.message
                _uiState.value = _uiState.value.copy(selectedRoom = null)
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateBookingStatusToCancel(bookingId: String) {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch

                val bookingRef = firestore.collection("Branch")
                    .document(uid)
                    .collection("Booking")
                    .document(bookingId)

                bookingRef.update("bookingStatus", "Cancel")
                    .addOnSuccessListener {
                        Log.d("BookingDebug", "Booking $bookingId updated to Cancel")
                    }
                    .addOnFailureListener { e ->
                        Log.e("BookingDebug", "Failed to update booking: ${e.message}")
                        _error.value = e.message
                    }

            } catch (e: Exception) {
                Log.e("BookingDebug", "Exception: ${e.message}")
                _error.value = e.message
            }
        }
    }

}


