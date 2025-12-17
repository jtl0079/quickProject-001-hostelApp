package com.example.hostelmanagement.report.presentation.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.hostelmanagement.booking.bookingHostel.FourButton
import com.example.hostelmanagement.report.infrastructure.dto.mapper.toPieChartDataList
import com.example.hostelmanagement.report.presentation.viewmodel.HostelReportViewModel
import com.myorg.kotlintools.android.ui.chart.PieChart
import com.myorg.kotlintools.android.ui.chart.PieChartData
import com.myorg.kotlintools.android.ui.selection.YearMonthRangeSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostelReportScreen(
    navController: NavController,
    viewModel: HostelReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val options = listOf(
        "Room Occupancy Rate",
        "Hostel Occupancy Rate"
    )

    val pieData = remember(
        uiState.reports,
        uiState.frequency,
        uiState.loading,
    ) {
        Log.d("PIEDATA", "${uiState.reports}")

        if (uiState.loading) {
            emptyList()
        } else {
            Log.d("PIEDATA", "${uiState.reports.data.keyTimeMap}")
            uiState.reports
                .data
                .keyTimeMap
                .toPieChartDataList(
                    frequency = uiState.frequency
                )
        }
    }

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDE2E4))
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {


        Text(
            text = "Report \n[${uiState.startYear}/${uiState.startMonth + 1} to ${uiState.endYear}/${uiState.endMonth + 1}]",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth(),
            color = Color.Black
        )

        ExposedDropdownMenuBox(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = uiState.selectedTarget,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categories") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            viewModel.onTargetChanged(selectionOption)
                            expanded = false
                        }
                    )
                }
            }
        }

        PieChart(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            dataInput = pieData,
            wordsInsidePanel = when {
                uiState.loading -> "Loading..."
                uiState.error != null -> "Error: ${uiState.error}"
                uiState.selectedTarget == "Room Occupancy Rate" -> "Times Per\n${uiState.frequency}"
                uiState.selectedTarget == "Hostel Occupancy Rate" -> "Times Per\n${uiState.frequency}"
                else -> "${pieData.size}"
            }


        )


        YearMonthRangeSelector(
            modifier = Modifier
                .fillMaxWidth(),
            onRangeChanged = { startY, startM, endY, endM, mode ->
                viewModel.onDateRangeChanged(
                    startYear = startY,
                    startMonth = startM,
                    endYear = endY,
                    endMonth = endM,
                    frequency = mode
                )
            }
        )

        FourButton(
            navController = navController,
            modifier = Modifier
        )


    }

}
