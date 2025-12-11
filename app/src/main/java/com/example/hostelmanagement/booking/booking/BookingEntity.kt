package com.example.hostelmanagement.booking.booking

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

enum class BookingStatus{Complete,Cancel}

@Entity(tableName = "booking")
data class BookingEntity (
    @PrimaryKey(autoGenerate = false) val bookingId:String = "",
    val bookingDate: LocalDate = LocalDate.now(),
    val bookedStartDate: LocalDate?=null,
    val bookedEndDate: LocalDate?=null,
    val bookingStatus: BookingStatus = BookingStatus.Complete,
    val tenantId:String = "",
    val roomId:String = ""
)

fun BookingEntity.toFirebase(): Map<String, Any?> {
    return mapOf(
        "bookingDate" to bookingDate.toString(),
        "bookedStartDate" to bookedStartDate?.toString(),
        "bookedEndDate" to bookedEndDate?.toString(),
        "bookingStatus" to bookingStatus.name,
        "tenantId" to tenantId,
        "roomId" to roomId
    )
}
