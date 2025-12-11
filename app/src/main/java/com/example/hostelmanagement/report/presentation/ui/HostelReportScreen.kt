package com.example.hostelmanagement.report.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myorg.kotlintools.android.ui.chart.PieChart
import com.myorg.kotlintools.android.ui.chart.RawData
import com.myorg.kotlintools.android.ui.selection.YearMonthRangeSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostelReportScreen(){

    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("选择一个选项") }

    val options = listOf("Each hostel occupancy rate", "Banana", "Orange")

    val data by remember { mutableStateOf(listOf<RawData>(
        RawData("餐饮", 1200.0),
        RawData("交通", 800.0),
        RawData("娱乐", 600.0),
        RawData("购物", 1500.0),
        RawData("房租", 3000.0),
        RawData("电子产品", 900.0),
        RawData("bathroom ", 900.0),
        RawData("0 开销", 900.0),
        RawData("1 开销", 900.0),
        RawData("其他", 900.0)
    )) }


    Column (
        modifier = Modifier.fillMaxSize()
    ){
        Text(

            text = "po",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }

        ) {
            // 这看起来就像 ComboBox
            TextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categories") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth().padding(8.dp)
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
                            selectedText = selectionOption
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
            dataInput = data
        )
        YearMonthRangeSelector(
            modifier = Modifier
                .fillMaxWidth()

        )
    }
}
