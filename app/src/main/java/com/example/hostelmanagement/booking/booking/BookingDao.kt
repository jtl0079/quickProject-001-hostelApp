package com.example.hostelmanagement.booking.booking

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookings: List<BookingEntity>)

    @Query("DELETE FROM booking WHERE bookingId = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM booking WHERE bookingStatus = :state ORDER BY bookingDate")
    fun getBookingUsingStatus(state: BookingStatus): Flow<List<BookingEntity>>

    @Query("SELECT * FROM booking ORDER BY bookingDate")
    fun getAllBooking(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM booking WHERE roomId = :roomId ORDER BY bookingDate")
    fun getBookingsForRoom(roomId: String): Flow<List<BookingEntity>>

    @Query("DELETE FROM booking")
    suspend fun deleteAllBookings()

}
