package com.example.hostelmanagement.booking.bookingRoom

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanagement.RoomItem
import com.example.hostelmanagement.booking.booking.BookingEntity
import com.example.hostelmanagement.booking.booking.BookingRepository
import com.example.hostelmanagement.booking.booking.BookingStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.collections.map
import kotlin.collections.sortedBy


@OptIn(FlowPreview::class)
class BookingRoomViewModel(
    private val roomDao: RoomRepository = RoomDao(),
    private val firestore: FirebaseFirestore= FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth =FirebaseAuth.getInstance(),
    private val repository: BookingRepository ,
) : ViewModel() {

    private val _sem = MutableStateFlow("")
    private val _durationYear = MutableStateFlow("")
    private val _durationMonth = MutableStateFlow("")
    private val _filter = MutableStateFlow(false)
    private val _availableRooms = MutableStateFlow<List<RoomItem>>(emptyList())
    private val _roomsState = MutableStateFlow<List<RoomItem>>(emptyList())
    val roomsState: StateFlow<List<RoomItem>> = _roomsState.asStateFlow()
    private val _loading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private var roomsListener: ListenerRegistration?=null
    private var currentObservedHostelId:String?=null
    var errorMessage by mutableStateOf("")
        private set

    private val _uiState = combine(
        _filter,
        _sem,
        _durationYear,
        _durationMonth,
        _availableRooms
    ) { filter, sem, durationYear,
        durationMonth, rooms ->
        BookingRoomUiState(
            filter = filter,
            sem = sem,
            durationYear = durationYear,
            durationMonth = durationMonth,
            room = emptyList(),
            maintenance = emptyList(),
            booking = emptyList(),
            validRoom = rooms
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, BookingRoomUiState())

    val uiState: StateFlow<BookingRoomUiState> = _uiState

    init {
        combine(_sem, _durationYear, _durationMonth) { sem, year, month ->
            Triple(sem, year, month)
        }
            .debounce(300)
            .distinctUntilChanged()
            .onEach { (sem, year, month) ->
                if (sem.isNotBlank() && (year.isNotBlank() || month.isNotBlank())) {
                    val booking = repository.getBookingUsingState(BookingStatus.Complete)
                    loadFilterState(booking)
                } else {
                    _availableRooms.value = emptyList()
                }
            }
            .launchIn(viewModelScope)
    }


    fun updateSem(newSem: String) {
        _sem.value = newSem
    }

    fun updateDurationYear(newDuration: String) {
        _durationYear.value = newDuration
    }

    fun updateDurationMonth(newDuration: String) {
        _durationMonth.value = newDuration
    }
    fun changeFilterState(status: Boolean) {
        _filter.value = status
    }

    private fun setError(msg: String) {
        errorMessage = msg
    }

    private fun showMessage(msg: String) {
        _error.value = msg
        errorMessage = msg
    }
    fun loadFilterState(booking :Flow<List<BookingEntity>>) {
        viewModelScope.launch {
            try {
                val room = try {
                    roomDao.getAvailableRooms(_sem.value,_durationYear.value,_durationMonth.value,booking,_roomsState.value)

                } catch (e: Exception) {
                    null
                }
                val roomsToUse: List<RoomItem> = room ?: run {
                    _roomsState.value
                }

                _availableRooms.value = roomsToUse
                _error.value = null
            } catch (e: Exception) {
                showMessage(e.message ?: "Failed to load rooms")
                _availableRooms.value = emptyList()
            }
        }

    }

    fun onFilterButtonClicked(sem: String, durationYear: String,durationMonth:String) {
        if (sem.isBlank()) {
            setError("Please select a Month Check-in")
            return
        }
        if (durationYear.isBlank() && durationMonth.isBlank()) {
            setError("Please select a Duration")
            return
        }
        if (durationYear=="3" && durationMonth=="12") {
            setError("Cant select 3 Year with 12 month")
            return
        }
        val booking = repository.getBookingUsingState(BookingStatus.Complete)
        loadFilterState(booking)
        changeFilterState(true)
    }

    fun observeRooms(hostelId:String){
        val uid= auth.currentUser?.uid ?:return
        val safeHostelId = hostelId
            .trim()
            .trim('/')
            .split('/')
            .lastOrNull() ?:return

        if(roomsListener!=null&&currentObservedHostelId==safeHostelId) {
            return
        }
        roomsListener?.remove()
        roomsListener=null
        currentObservedHostelId=safeHostelId

        val coll = firestore.collection("Branch")
            .document(uid)
            .collection("Hostel")
            .document(safeHostelId)
            .collection("rooms")

        _loading.value=true
        _error.value=null

        roomsListener=coll.addSnapshotListener { snaps,e ->
            if (e!=null){
                _error.value=e.message
                _loading.value=false
                return@addSnapshotListener
            }
            if (snaps!=null) {
                val list = snaps.documents.map { d ->
                    RoomItem(
                        roomId = d.getString("roomId") ?: d.id,
                        photoUrl = d.getString("photoUrl"),
                        roomType = d.getString("roomType"),
                        roomCapacity = d.getLong("roomCapacity")?.toInt(),
                        roomPrice = d.getDouble("roomPrice"),
                        roomDescription = d.getString("roomDescription"),
                        createdAt = d.getTimestamp("createdAt"),
                        roomStatus = d.getString("roomStatus")?:"Available"
                    )
                }.sortedBy { it.roomId }
                _roomsState.value = list
            }else{
                _roomsState.value= emptyList()
            }
            _loading.value=false
        }
    }

}
