package com.example.hostelmanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hostelmanagement.maintenance.MaintenanceViewModel
import kotlinx.coroutines.launch
import kotlin.text.toDoubleOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun RoomEditPage(
    hostelId:String,
    room:RoomItem,
    onDone:()->Unit,
    viewModel: RoomViewModel= viewModel(),
    onMaintenanceRequest:()->Unit,
    onMaintenanceRecords:()->Unit,
    maintenanceViewModel: MaintenanceViewModel
){
    var priceText by remember { mutableStateOf(room.roomPrice?.toString()?:"") }
    var descriptionText by remember { mutableStateOf(room.roomDescription?:"") }
    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit){
        maintenanceViewModel.selectedRoomId.value = room.roomId
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFFDE2E4),
        topBar = {
            Column(
                modifier=Modifier.fillMaxWidth().background(Color(0xFFFDE2E4))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 8.dp, top = 25.dp, bottom = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    IconButton(onClick = {
                        if (!isSaving) onDone()
                        maintenanceViewModel.clearRoomId()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                Text(
                    "Edit Room",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize=22.sp,
                    color = Color(0xFF634035))
            } },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {

                if (room.photoUrl.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Photo",
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    AsyncImage(
                        model = room.photoUrl,
                        contentDescription = "Room Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Room ID: ${room.roomId}", color = Color(0xFF634035), fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Type: ${room.roomType}", color = Color(0xFF634035), fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Capacity: ${room.roomCapacity}", color = Color(0xFF634035), fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Price(RM)") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = descriptionText,
                onValueChange = { descriptionText = it },
                label = { Text("Description") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                ),
                modifier = Modifier.fillMaxWidth().height(120.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                Column(
                    modifier=Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (isSaving) return@Button

                            val roomPrice = priceText.toDoubleOrNull()
                            if (roomPrice == null || roomPrice <= 0.0) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter a valid price.") }
                                return@Button
                            }
                            isSaving = true
                            viewModel.updateRoom(
                                hostelId = hostelId,
                                roomId = room.roomId,
                                roomPrice = roomPrice,
                                roomDescription = descriptionText,
                                onSuccess = {
                                    isSaving = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Room updated successfully.")
                                        onDone()
                                    }
                                },
                                onError = { errorMsg ->
                                    isSaving = false
                                    scope.launch { snackbarHostState.showSnackbar("Error: $errorMsg") }
                                }
                            )
                        },
                        modifier = Modifier.width(180.dp).shadow(4.dp, RoundedCornerShape(16.dp)),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9DFCFF),
                            contentColor = Color(0xFF634035)
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        } else {
                            Text(text = "Update")
                        }
                    }
                    Button(
                        onClick = { if (!isSaving) onDone() },
                        modifier = Modifier.width(180.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB89278),
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Cancel")
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onMaintenanceRequest() },
                        modifier = Modifier.width(180.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF634035),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Maintenance Request")
                    }
                    Button(
                        onClick = { onMaintenanceRecords() },
                        modifier = Modifier.width(180.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF634035),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Maintenance Records")
                    }
                }
            }
        }
    }
}
