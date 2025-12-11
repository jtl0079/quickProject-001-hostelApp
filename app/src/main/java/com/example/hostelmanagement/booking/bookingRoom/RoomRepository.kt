package com.example.hostelmanagement.booking.bookingRoom

import com.example.hostelmanagement.RoomItem
import com.example.hostelmanagement.booking.booking.BookingEntity
import kotlinx.coroutines.flow.Flow


interface RoomRepository {
    suspend fun getAvailableRooms(
        sem: String,
        durationYear: String,
        durationMonth: String,
        booking: Flow<List<BookingEntity>>,
        room: List<RoomItem>
    ): List<RoomItem>

    suspend fun countDuration(
        sem: String,
        durationYear: String,
        durationMonth: String,
    ):List<String>
}
