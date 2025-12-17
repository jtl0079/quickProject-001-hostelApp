package com.example.hostelmanagement.report.domain.model

import androidx.compose.ui.unit.IntSize
import com.myorg.kotlintools.time.domain.model.alltime.AllTimeDataBundle
import com.myorg.kotlintools.time.domain.model.instant.InstantlyEntries
import com.myorg.kotlintools.time.domain.model.instant.InstantlyEntry
import com.myorg.kotlintools.valueOperatorOf
import java.time.Instant

/*

reportId : "RP2025-1212-000001"
reportName : "",
periodStartDate: 1733549212345,
periodEndDate : 1733549212345,
frequency : "monthly",      // monthly, yearly
target : "total income",         // occupancy, income, maintenance
    val data: AllTimeDataBundle<String, Double>,
data = {
    entries: [
    { key:"roomA", timestamp: 1733549212345, value: 80.5 },
    { key:"roomA", timestamp: 1733635612345, value: 82.0 }
    ],
}
branchId : BH000001
*/
data class HostelReport(
    val reportId: String = "",
    val reportName: String = "",
    val periodStartDate: Instant = Instant.now(),
    val periodEndDate: Instant = Instant.now(),
    val frequency: String ="month",      // month, year
    val target: String = "occupancy",         // occupancy, income, maintenance
    val data: AllTimeDataBundle<String, Double> = AllTimeDataBundle(timeEntries = InstantlyEntries(valueOperator = valueOperatorOf<Double>())),
    val branchId: String = ""
)

fun DemoHostelReport(): HostelReport {
    val data =
        AllTimeDataBundle<String, Double>(
            timeEntries = InstantlyEntries(valueOperator = valueOperatorOf<Double>())
        )

    data.timeEntries.entries.add(
        InstantlyEntry("table", Instant.now(), 1234.0)
    )
    val report = HostelReport(
        reportId = "r00001_Demo",
        reportName = "January Occupancy",
        periodStartDate = Instant.parse("2025-01-01T00:00:00Z"),
        periodEndDate = Instant.parse("2025-01-31T00:00:00Z"),
        frequency = "monthly",
        target = "occupancy",
        data = data,
        branchId = "B001"
    )

    return report
}

