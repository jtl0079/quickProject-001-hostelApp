package com.example.hostelmanagement.booking.tenantInformation

import android.service.notification.Condition.newId
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanagement.RoomItem
import com.example.hostelmanagement.booking.booking.BookingStatus
import com.example.hostelmanagement.booking.bookingRoom.RoomDao
import com.example.hostelmanagement.booking.bookingRoom.RoomRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class TenantInformationViewModel(
    private val firestore: FirebaseFirestore= FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth =FirebaseAuth.getInstance(),
    private val roomDao: RoomRepository = RoomDao()
): ViewModel() {

    val tenant = listOf(
        TenantEntity("T001","aaa","aaa@gmail.com","0123456789"),
        TenantEntity("T002","bbb","bbb@gmail.com","0123456780"),
        TenantEntity("T003","ccc","ccc@gmail.com","0123456781"),
        TenantEntity("T004","ddd","ddd@gmail.com","0123456782"),
        TenantEntity("T005","eee","eee@gmail.com","0123456783"),
        TenantEntity("T006","fff","fff@gmail.com","0123456784"),
        TenantEntity("T007","ggg","ggg@gmail.com","0123456785"),
        TenantEntity("T008","hhh","hhh@gmail.com","0123456786"),
        TenantEntity("T009","iii","iii@gmail.com","0123456787"),
        TenantEntity("T010","jjj","jjj@gmail.com","0123456788"),
    )
    private val _uiState = MutableStateFlow(TenantInformationUiState())
    val uiState: StateFlow<TenantInformationUiState> = _uiState
    private var roomsListener: ListenerRegistration?=null
    private var currentObservedHostelId:String?=null
    private var tenantListener: ListenerRegistration?=null
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _addLoading = MutableStateFlow(false)
    val addLoading = _addLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _bookingSuccess = MutableSharedFlow<String>(replay = 0)
    val bookingSuccess: SharedFlow<String> = _bookingSuccess.asSharedFlow()
    private val _bookingError = MutableSharedFlow<String>(replay = 0)
    val bookingError: SharedFlow<String> = _bookingError.asSharedFlow()

    private val _roomsState = MutableStateFlow<List<RoomItem>>(emptyList())
    val roomsState: StateFlow<List<RoomItem>> = _roomsState.asStateFlow()

    fun clearError(){
        _uiState.update { it.copy(errorMessage = "") }
    }
    suspend fun getDuration(sem:String, year:String, month:String ){
        _uiState.update { it.copy(duration = roomDao.countDuration(sem,year,month)) }
    }
    fun onNameChange(value: String) {
        _uiState.update { it.copy(tenantName = value) }
    }
    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value) }
    }
    fun onIdChange(value: String) {
        _uiState.update { it.copy(tenantId = value) }
        observeTenant()
        for(tenant in _uiState.value.tenant){
            if(tenant.tenantId == value){
                onNameChange(tenant.tenantName)
                onEmailChange(tenant.tenantEmail)
                onPhoneNumberChange(tenant.tenantPhoneNumber)
            }
        }
    }
    suspend fun generateIdWithPrefix(prefix: String, collectionRef: CollectionReference): String {
        val snapshot = collectionRef.get().await()

        val maxNum = snapshot.documents.mapNotNull { doc ->
            val id = doc.id
            if (id.startsWith(prefix, ignoreCase = true)) {
                id.removePrefix(prefix).toIntOrNull()
            } else null
        }.maxOrNull() ?: 0

        val nextNum = maxNum + 1
        return prefix + String.format("%04d", nextNum)
    }

    fun addBookingAsync(roomId: String) {
        viewModelScope.launch {
            _addLoading.value = true
            try {
                val bookingId = addBooking(roomId)
                _bookingSuccess.emit(bookingId)
            } catch (e: Exception) {
                val msg = e.message ?: "Unknown error"
                _bookingError.emit(msg)
                _error.value = msg
            } finally {
                _addLoading.value = false
            }
        }
    }

    suspend fun addBooking(roomId:String):String {
        val uid = auth.currentUser?.uid ?: return "Not login stage"

        val bookingCollection = firestore.collection("Branch")
            .document(uid)
            .collection("Booking")

        val newBookingId = generateIdWithPrefix("BK", bookingCollection)
        val now = LocalDate.now().toString()
        val data = mapOf(
            "BookingId" to newBookingId,
            "bookingDate" to now,
            "bookedStartDate" to uiState.value.duration[0],
            "bookedEndDate" to uiState.value.duration[1],
            "bookingStatus" to BookingStatus.Complete,
            "tenantId" to uiState.value.tenantId,
            "roomId" to roomId
        )
        bookingCollection.document(newBookingId).set(data).await()
        return newBookingId
    }
    fun onPhoneNumberChange(value: String) {
        _uiState.update { it.copy(phoneNumber = value) }
    }
    fun observeTenant() {
        val uid = auth.currentUser?.uid ?: return

        val coll = firestore.collection("Branch")
            .document(uid)
            .collection("Tenant")

        _loading.value = true
        _error.value = null

        tenantListener?.remove()
        tenantListener = coll.addSnapshotListener { snaps, e ->
            if (e != null) {
                _error.value = e.message
                _loading.value = false
                return@addSnapshotListener
            }

            if (snaps != null) {
                if (snaps.isEmpty) {
                    val defaultTenants = tenant

                    val batch = firestore.batch()
                    for (t in defaultTenants) {
                        val docRef = coll.document(t.tenantId)
                        val map = hashMapOf(
                            "tenantId" to t.tenantId,
                            "tenantName" to t.tenantName,
                            "tenantPhone" to t.tenantPhoneNumber,
                            "tenantEmail" to t.tenantEmail
                        )
                        batch.set(docRef, map)
                    }

                    batch.commit()
                        .addOnSuccessListener {
                            _uiState.update { it.copy(tenant = defaultTenants) }
                            _loading.value = false
                        }
                        .addOnFailureListener { ex ->
                            _error.value = ex.message
                            _loading.value = false
                        }

                    return@addSnapshotListener
                }

                val list = snaps.documents.map { d ->
                    TenantEntity(
                        tenantId = d.getString("tenantId") ?: d.id,
                        tenantName = d.getString("tenantName") ?: "",
                        tenantPhoneNumber = d.getString("tenantPhone") ?: "",
                        tenantEmail = d.getString("tenantEmail") ?: ""
                    )
                }

                _uiState.update { it.copy(tenant = list) }

            } else {
                _uiState.update { it.copy(tenant = emptyList()) }
            }

            _loading.value = false
        }
    }




    fun observeRooms(hostelId: String, roomId: String?) {
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
                val list = snaps.documents.mapNotNull { d ->
                    val docRoomId = d.getString("roomId") ?: d.id

                    if (roomId != null && roomId.isNotBlank()) {
                        if (docRoomId != roomId) return@mapNotNull null
                    }

                    RoomItem(
                        roomId = docRoomId,
                        photoUrl = d.getString("photoUrl"),
                        roomType = d.getString("roomType"),
                        roomCapacity = d.getLong("roomCapacity")?.toInt(),
                        roomPrice = d.getDouble("roomPrice"),
                        roomDescription = d.getString("roomDescription"),
                        roomStatus = d.getString("roomStatus") ?: "Available"
                    )
                }
                _roomsState.value = list
            } else {
                _roomsState.value = emptyList()
            }
            _loading.value = false
        }
    }

}