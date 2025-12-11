package com.example.hostelmanagement.booking.bookingRoom

import com.example.hostelmanagement.RoomItem
import com.example.hostelmanagement.booking.booking.BookingEntity
import com.example.hostelmanagement.maintenance.Maintenance

data class BookingRoomUiState(
    val filter: Boolean = true,
    val sem: String = "",
    val durationYear: String = "",
    val durationMonth: String = "",
    val room: List<RoomItem> = emptyList(),
    val maintenance: List<Maintenance> = emptyList(),
    val booking: List<BookingEntity> = emptyList(),
    val validRoom:List<RoomItem> = emptyList()
)


