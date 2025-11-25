package com.example.miniproject

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.miniproject.ui.theme.MiniProjectTheme
import com.google.firebase.auth.FirebaseAuth
import com.myorg.kotlintools.MonthlyReport
import com.myorg.kotlintools.MonthlyReportOf
import com.myorg.kotlintools.VibrateUtils
import com.myorg.kotlintools.composable.*
import java.time.Instant


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            MiniProjectTheme {

                val context = LocalContext.current

                val report = remember { MonthlyReportOf<Int>() }
                var version by remember { mutableIntStateOf(0) }
                var user by remember{ mutableStateOf(FirebaseAuth.getInstance().currentUser.toString()) }


                Column {
                    DatePickerOutlinedButton()
                    DatePicker()
                    YearMonthPicker()
                    Button(onClick = {
                        report.addIncremental("A", 10, Instant.now())
                        version++
                        VibrateUtils.vibrate(context, 1000)
                    }) {
                        Text(
                            text = "${report.getSumOfKey("A").toString()} $version ${user}"
                        )
                    }
                    FirebaseDefaultLoginComponent()

                }


            }

        }
    }
}


