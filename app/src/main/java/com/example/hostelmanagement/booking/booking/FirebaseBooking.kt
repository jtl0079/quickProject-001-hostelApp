package com.example.hostelmanagement.booking.booking

import java.time.LocalDate

data class FirebaseBooking(
    val bookingDate: String? = null,
    val bookedStartDate: String? = null,
    val bookedEndDate: String? = null,
    val bookingStatus: String? = null,
    val tenantId: String? = null,
    val roomId: String? = null
) {
    fun toEntity(docId: String): BookingEntity {
        return BookingEntity(
            bookingId = docId,
            bookingDate = bookingDate?.let { LocalDate.parse(it) } ?: LocalDate.now(),
            bookedStartDate = bookedStartDate?.let { LocalDate.parse(it) },
            bookedEndDate = bookedEndDate?.let { LocalDate.parse(it) },
            bookingStatus = try {
                BookingStatus.valueOf(bookingStatus ?: BookingStatus.Complete.name)
            } catch (e: Exception) {
                BookingStatus.Complete
            },
            tenantId = tenantId ?: "",
            roomId = roomId ?: ""
        )
    }
}
