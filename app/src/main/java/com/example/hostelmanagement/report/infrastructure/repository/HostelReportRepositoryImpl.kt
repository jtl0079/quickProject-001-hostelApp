package com.example.hostelmanagement.report.infrastructure.repository

import android.util.Log
import com.example.hostelmanagement.booking.booking.BookingEntity
import com.example.hostelmanagement.booking.booking.BookingRepository
import com.example.hostelmanagement.report.domain.model.HostelReport
import com.example.hostelmanagement.report.domain.repository.HostelReportRepository
import com.example.hostelmanagement.report.infrastructure.datasource.HostelDataSource
import com.example.hostelmanagement.report.infrastructure.datasource.HostelReportDataSourceFirestore
import com.example.hostelmanagement.report.infrastructure.datasource.RoomDataSource
import com.example.hostelmanagement.report.infrastructure.dto.mapper.toDomain
import com.example.hostelmanagement.report.infrastructure.dto.mapper.toDto
import com.myorg.kotlintools.time.domain.model.alltime.AllTimeDataBundle
import com.myorg.kotlintools.time.domain.model.instant.InstantlyEntries
import com.myorg.kotlintools.time.domain.model.instant.InstantlyEntry
import com.myorg.kotlintools.time.utils.countPeriodDurationDays
import com.myorg.kotlintools.valueOperatorOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit


class HostelReportRepositoryImpl(
    private val hostelReportDataSourceFirestore: HostelReportDataSourceFirestore = HostelReportDataSourceFirestore(),
    private val bookingRepo: BookingRepository
) : HostelReportRepository {


    override fun observeReportsByBranch(
        branchId: String,
        frequency: String,
        target: String,
        startYear: Int,
        startMonth: Int,
        endYear: Int,
        endMonth: Int
    ): Flow<HostelReport> {

        val reportStart = LocalDate.of(startYear, startMonth, 1)
        val reportEnd = LocalDate.of(endYear, endMonth, 1)


        Log.d("REPORT_REPO", "observeReportsByBranch() called")

        val reportFlow: Flow<HostelReport?> =
            hostelReportDataSourceFirestore
                .observeReports(
                    branchId = branchId,
                    frequency = frequency,
                    startYear = startYear,
                    startMonth = startMonth,
                    endYear = endYear,
                    endMonth = endMonth
                )
                .map { dto ->
                    Log.d("REPORT_REPO", "Firestore emit: dto = $dto")
                    dto?.toDomain()
                }

        val bookingFlow: Flow<List<BookingEntity>> =
            bookingRepo.getAllBooking()
                .map { bookings ->
                    Log.d("REPORT_REPO", "Booking emit: size=${bookings.size}")
                    bookings
                }

        return combine(reportFlow, bookingFlow) { existingReport, bookings ->

            Log.d(
                "REPORT_REPO",
                "combine triggered | report=${existingReport != null}, bookings=${bookings.size}"
            )

            // A️⃣ Firestore 已有
            if (existingReport != null) {
                Log.d("REPORT_REPO", "Using Firestore report")
                return@combine existingReport
            }

            // B️⃣ Firestore 没有 → 动态生成
            Log.d("REPORT_REPO", "No report in Firestore, generating new report")

            val bundle =
                AllTimeDataBundle<String, Double>(
                    timeEntries = InstantlyEntries(valueOperator = valueOperatorOf<Double>())
                )


            when (target) {

                "Room Occupancy Rate" -> {
                    Log.d("REPORT_REPO", "Generating Room Occupancy Rate report")

                    writeRoomOccupancyToBundle(
                        bundle = bundle,
                        bookings = bookings,
                        reportStart = reportStart,
                        reportEnd = reportEnd,
                        frequency = frequency
                    )
                }
                "Hostel Occupancy Rate"->{
                    Log.d("REPORT_REPO", "Generating Hostel Occupancy Rate report")

                    writeHostelOccupancyToBundle(
                        bundle = bundle,
                        bookings = bookings,
                        reportStart = reportStart,
                        reportEnd = reportEnd,
                        frequency = frequency
                    )
                }

                else -> {
                    Log.w(
                        "REPORT_REPO",
                        "Unsupported target=$target, bundle will be empty"
                    )
                }
            }


            Log.d(
                "REPORT_REPO",
                "Generated bundle size = ${bundle.keyTimeMap.keyTimeMap.size}"
            )

            HostelReport(
                reportId = "",
                reportName = "${target}_${startYear}_${startMonth}_${endYear}_${endMonth}",
                periodStartDate = reportStart
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant(),
                periodEndDate = reportEnd
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant(),
                frequency = frequency,
                target = target,
                data = bundle,
                branchId = branchId
            )
        }
    }


    override suspend fun saveReport(report: HostelReport) {
        val dto = report.toDto()
        Log.d(
            "REPORT_REPO",
            "saveReport() called | dto = $dto"
        )
        hostelReportDataSourceFirestore.uploadReport(report.branchId, dto)
    }

    suspend fun deleteReport(reportId: String) {
        hostelReportDataSourceFirestore.deleteReport(reportId)
    }
}









suspend fun writeRoomOccupancyToBundle(
    bundle: AllTimeDataBundle<String, Double>,
    bookings: List<BookingEntity>,
    reportStart: LocalDate,
    reportEnd: LocalDate,
    frequency: String // "month" | "year"
) {
    val zoneId = ZoneId.systemDefault()
    val startInstant = reportStart.atStartOfDay(zoneId).toInstant()

    /* =======================
       1️⃣ 预加载 hostel & room
       ======================= */
    val hostels = HostelDataSource.loadAllHostelsInBranch()

    // roomId -> hostelName
    val roomHostelMap = mutableMapOf<String, String>()

    // roomKey -> occupied dates
    val roomOccupiedDates = mutableMapOf<String, MutableSet<LocalDate>>()

    hostels.forEach { hostel ->
        val rooms = RoomDataSource.loadRoomsByHostel(hostel.hostelId)
        rooms.forEach { room ->
            val key = "${hostel.hostelName} ${room.roomId}"
            roomHostelMap[room.roomId] = hostel.hostelName
            roomOccupiedDates[key] = mutableSetOf()

            // 初始化 key
            bundle.addEntry(InstantlyEntry(key, startInstant, 0.0))
            bundle.addEntry(InstantlyEntry("$key (unoccupied)", startInstant, 0.0))
        }
    }

    /* =======================
       2️⃣ 收集 occupied dates
       ======================= */
    bookings.forEach { booking ->
        val hostelName = roomHostelMap[booking.roomId] ?: return@forEach
        val key = "$hostelName ${booking.roomId}"

        val bookingStart = booking.bookedStartDate ?: return@forEach
        val bookingEnd = booking.bookedEndDate ?: return@forEach

        val effectiveStart =
            if (bookingStart.isAfter(reportStart)) bookingStart else reportStart

        val effectiveEnd =
            if (bookingEnd.isBefore(reportEnd)) bookingEnd else reportEnd

        if (effectiveStart.isAfter(effectiveEnd)) return@forEach

        var d = effectiveStart
        while (d.isBefore(effectiveEnd)) {
            roomOccupiedDates[key]?.add(d)
            d = d.plusDays(1)
        }
    }

    /* =======================
       3️⃣ 写入 occupied days
       ======================= */
    roomOccupiedDates.forEach { (key, dates) ->
        bundle.addEntry(
            InstantlyEntry(
                key = key,
                timestamp = startInstant,
                value = dates.size.toDouble()
            )
        )
    }

    bundle.syncMapByEntries()

    /* =======================
       4️⃣ 计算 unoccupied
       ======================= */
    val totalDays = countPeriodDurationDays(reportStart, reportEnd).toDouble()

    bundle.keyTimeMap.keyTimeMap.forEach { (key, group) ->
        if (key.endsWith(" (unoccupied)")) return@forEach

        val occupiedDays = when (frequency.lowercase().trim()) {
            "month" -> group.monthlyMap.timeMap.values.sum()
            "year"  -> group.yearlyMap.timeMap.values.sum()
            else    -> 0.0
        }

        val unoccupiedDays =
            if (totalDays - occupiedDays < 0) 0.0 else totalDays - occupiedDays

        bundle.addEntry(
            InstantlyEntry(
                key = "$key (unoccupied)",
                timestamp = startInstant,
                value = unoccupiedDays
            )
        )
    }

    bundle.syncMapByEntries()
}

suspend fun writeHostelOccupancyToBundle(
    bundle: AllTimeDataBundle<String, Double>,
    bookings: List<BookingEntity>,
    reportStart: LocalDate,
    reportEnd: LocalDate,
    frequency: String
) {
    val zoneId = ZoneId.systemDefault()
    val startInstant = reportStart.atStartOfDay(zoneId).toInstant()

    /* =======================
       1️⃣ 先复用 room 逻辑
       ======================= */
    val roomBundle =
        AllTimeDataBundle<String, Double>(
            timeEntries = InstantlyEntries(valueOperator = valueOperatorOf())
        )

    writeRoomOccupancyToBundle(
        bundle = roomBundle,
        bookings = bookings,
        reportStart = reportStart,
        reportEnd = reportEnd,
        frequency = frequency
    )

    /* =======================
       2️⃣ room → hostel 聚合
       ======================= */
    val hostelSum = mutableMapOf<String, Double>()

    roomBundle.keyTimeMap.keyTimeMap.forEach { (key, group) ->
        // key 例子：
        // "HostelA R101"
        // "HostelA R101 (unoccupied)"

        val isUnoccupied = key.endsWith(" (unoccupied)")
        val hostelName = key
            .removeSuffix(" (unoccupied)")
            .substringBefore(" ")

        val value = when (frequency.lowercase().trim()) {
            "month" -> group.monthlyMap.timeMap.values.sum()
            "year"  -> group.yearlyMap.timeMap.values.sum()
            else    -> 0.0
        }

        val hostelKey =
            if (isUnoccupied) "$hostelName (unoccupied)" else hostelName

        hostelSum[hostelKey] =
            hostelSum.getOrDefault(hostelKey, 0.0) + value
    }

    /* =======================
       3️⃣ 写入 hostel bundle
       ======================= */
    hostelSum.forEach { (key, value) ->
        bundle.addEntry(
            InstantlyEntry(
                key = key,
                timestamp = startInstant,
                value = value
            )
        )
    }

    bundle.syncMapByEntries()
}
