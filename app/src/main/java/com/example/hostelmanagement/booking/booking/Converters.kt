package com.example.hostelmanagement.booking.booking

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateConverter {
    @TypeConverter
    fun localDateToString(date: LocalDate?):String{
        return date.toString()
    }

    @TypeConverter
    fun stringToLocalDate(date:String?): LocalDate? {
        return date?.let {
            LocalDate.parse(it)
        }
    }
}
class BookingStatusConverter {
    @TypeConverter
    fun statusToString(status: BookingStatus): String = status.name

    @TypeConverter
    fun stringToStatus(value: String): BookingStatus = BookingStatus.valueOf(value)
}