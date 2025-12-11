package com.example.hostelmanagement.booking.bookingRoom

import com.example.hostelmanagement.RoomItem
import com.example.hostelmanagement.booking.booking.BookingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import kotlin.collections.mutableListOf

class RoomDao : RoomRepository {

    override suspend fun getAvailableRooms(
        sem: String,
        durationYear: String,
        durationMonth: String,
        booking: Flow<List<BookingEntity>>,
        room: List<RoomItem>
    ): List<RoomItem> {
        val room = getRoom(room)
        val availableRooms = mutableListOf<RoomItem>()
        val year:Long = duration(durationYear)
        val month: Long = duration(durationMonth)
        val startDate = semToStartDate(sem)
        val endDate = startDate.plusMonths(month).plusYears(year)
        val existingBookings = booking.first()


        for (r in room) {

            val bookingsForRoom = existingBookings.filter { b -> b.roomId == r.roomId }

            val hasOverlap = bookingsForRoom.any { b ->
                isOverlapping(startDate, endDate, b.bookedStartDate, b.bookedEndDate)
            }

            if (!hasOverlap) {
                availableRooms.add(r)
            }
        }
        return availableRooms
    }

    override suspend fun countDuration(
        sem: String,
        durationYear: String,
        durationMonth: String
    ): List<String> {
        val semYearMonth = mutableListOf<String>()
        val year:Long = duration(durationYear)
        val month: Long = duration(durationMonth)
        val startDate = semToStartDate(sem)
        semYearMonth.add(semToStartDate(sem).toString())
        val endDate = startDate.plusMonths(month).plusYears(year)
        semYearMonth.add(endDate.toString())

        return semYearMonth
    }

    fun getRoom(rooms: List<RoomItem>): List<RoomItem>{
        val list = mutableListOf<RoomItem>()
        for(room in rooms) {
            if(room.roomStatus == "Available"){
                list.add(room)
            }
        }
        return list
    }

    fun isOverlapping(bookingStartDate: LocalDate, bookingEndDate: LocalDate, recordStart: LocalDate?, recordEnd: LocalDate?): Boolean {
        return !(bookingEndDate.isBefore(recordStart) || bookingStartDate.isAfter(recordEnd))
    }

    fun semToStartDate(sem: String): LocalDate {
        val now = java.time.LocalDate.now()
        val year = java.time.LocalDate.now().year
        val january = LocalDate.of(year, 1, 1)
        val june = LocalDate.of(year, 6, 1)
        val october = LocalDate.of(year, 10, 1)

        return when (sem.trim().lowercase()) {
            "january" -> if(january.isBefore(now)){
                january.plusYears(1)
            }else{
                january
            }
            "june" -> if(june.isBefore(now)){
                june.plusYears(1)
            }else{
                june
            }
            "october" -> if(october.isBefore(now)){
                october.plusYears(1)
            }else{
                october
            }
            else -> LocalDate.of(year, 1, 1)
        }
    }

    fun duration(duration: String): Long {
        if(duration == "None")return 0
        val number : Long = duration.filter { it.isDigit() }.toLongOrNull() ?: 0
        return number
    }
}