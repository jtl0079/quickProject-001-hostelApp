package com.example.hostelmanagement.booking

import android.app.Application
import androidx.room.Room
import com.example.hostelmanagement.booking.booking.BookingRepository


class App : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "hostel.db").build()
    }

    val bookingRepository: BookingRepository by lazy {
        BookingRepository(database.bookingDao())
    }
}