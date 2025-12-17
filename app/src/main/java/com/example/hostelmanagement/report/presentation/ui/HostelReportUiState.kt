package com.example.hostelmanagement.report.presentation.ui

import com.example.hostelmanagement.report.domain.model.HostelReport
import com.myorg.kotlintools.android.ui.chart.PieChartData


data class HostelReportUiState(
    val loading: Boolean = false,
    val reports: HostelReport = HostelReport(),
    val error: String? = null,


    val selectedTarget: String = "Room Occupancy Rate",
    val frequency: String = "month",
    val startYear: Int = 0,
    val startMonth: Int = 0,
    val endYear: Int = 0,
    val endMonth: Int = 0,

    val pieChartData: List<PieChartData> = emptyList()
)
