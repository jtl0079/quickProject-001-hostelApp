package com.example.hostelmanagement.booking.bookingHistory

import com.example.hostelmanagement.RoomItem
import com.example.hostelmanagement.booking.booking.BookingEntity

data class BookingHistoryUiState(

    val completeBooked:List<BookingEntity> = emptyList(),
    val cancelBooked: List<BookingEntity> = emptyList(),
    val lastFoundHostelId:String = "",
    val room: List<RoomItem> = emptyList(),
    val selectedRoom: RoomItem? = null
)