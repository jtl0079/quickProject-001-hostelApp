package com.example.miniproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.miniproject.kotlinTools.Composable.FirebaseDefaultLoginComponent
import com.example.miniproject.kotlinTools.Composable.TimePicker
import com.example.miniproject.ui.theme.MiniProjectTheme
import com.example.project.kotlinTools.PieChart
import com.example.project.kotlinTools.RawData
import com.myorg.kotlintools.composable.RawData


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            MiniProjectTheme {

                val dataList = remember {
                    mutableStateListOf<RawData>(
                        RawData("first", 360.0),
                        RawData("second", 360.0),
                        RawData(name = "thrid", value = 360.0),
                        RawData(name = "forth", value = 360.0),
                        RawData(name = "fifth", value = 360.0),
                    )
                }

                var selectedTime by remember { mutableStateOf("未选择") }

                var hour by rememberSaveable { mutableIntStateOf(12) }
                var minute by rememberSaveable { mutableIntStateOf(30) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {

                                // ⭐ 这里一定可以用 indexOfFirst
                                val index = dataList.indexOfFirst { it.name == "first" }

                                if (index != -1) {
                                    val old = dataList[index]
                                    dataList[index] = old.copy(value = old.value + 10.0)
                                }
                            }
                        ) {
                            Text("+1")
                        }
                    }
                ) { innerPadding ->
                    val modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()


                    TimePicker(
                        onTimeSelected = { hour, minute ->
                            selectedTime = "%02d:%02d".format(hour, minute)
                        },
                        content = { formatedTime , onClick ->
                            OutlinedButton(onClick = onClick) {
                                Icon(Icons.Default.AccessTime, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(formatedTime)
                            }

                        }
                    )

                    Text("${selectedTime}")
                }
            }

        }
    }
}
