package com.example.hostelmanagement.booking

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hostelmanagement.booking.booking.BookingDao
import com.example.hostelmanagement.booking.booking.BookingEntity
import com.example.hostelmanagement.booking.booking.BookingStatusConverter
import com.example.hostelmanagement.booking.booking.LocalDateConverter

@Database(
    entities = [BookingEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(BookingStatusConverter::class, LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDao
}