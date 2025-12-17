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
        val february = LocalDate.of(year,2,1)
        val march = LocalDate.of(year, 3, 1)
        val april = LocalDate.of(year,4,1)
        val may = LocalDate.of(year, 5, 1)
        val june = LocalDate.of(year, 6, 1)
        val july = LocalDate.of(year,7,1)
        val august = LocalDate.of(year, 8, 1)
        val september = LocalDate.of(year, 9, 1)
        val october = LocalDate.of(year, 10, 1)
        val november = LocalDate.of(year,11,1)
        val december = LocalDate.of(year, 12, 1)

        return when (sem.trim().lowercase()) {
            "january" -> if(january.isBefore(now)){
                january.plusYears(1)
            }else{
                january
            }
            "february" -> if(february.isBefore(now)){
                february.plusYears(1)
            }else{
                february
            }
            "march" -> if(march.isBefore(now)){
                march.plusYears(1)
            }else{
                march
            }
            "april" -> if(april.isBefore(now)){
                april.plusYears(1)
            }else{
                april
            }
            "may" -> if(may.isBefore(now)){
                may.plusYears(1)
            }else{
                may
            }
            "june" -> if(june.isBefore(now)){
                june.plusYears(1)
            }else{
                june
            }
            "july" -> if(july.isBefore(now)){
                july.plusYears(1)
            }else{
                july
            }
            "august" -> if(august.isBefore(now)){
                august.plusYears(1)
            }else{
                august
            }
            "september" -> if(september.isBefore(now)){
                september.plusYears(1)
            }else{
                september
            }
            "october" -> if(october.isBefore(now)){
                october.plusYears(1)
            }else{
                october
            }
            "november" -> if(november.isBefore(now)){
                november.plusYears(1)
            }else{
                november
            }
            "december" -> if(december.isBefore(now)){
                december.plusYears(1)
            }else{
                december
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