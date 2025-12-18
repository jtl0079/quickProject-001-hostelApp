package com.example.hostelmanagement.booking.tenantInformation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import kotlinx.coroutines.launch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.CoroutineScope

@Composable
fun TenantInformationScreen(
    hostelId:String, roomId:String, sem:String,
    year:String, month:String, navController: NavController,
    viewModel: TenantInformationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val addLoading by viewModel.addLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(hostelId, roomId) {
        viewModel.observeTenant()
        viewModel.getDuration(sem,year,month)
        viewModel.observeRooms(hostelId,roomId)
    }

    LaunchedEffect(viewModel) {
        viewModel.bookingSuccess.collect { bookingId ->
            scope.launch {
                snackbarHostState.showSnackbar("Booking $bookingId successful")
                navController.popBackStack()
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.bookingError.collect { msg ->
            scope.launch {
                snackbarHostState.showSnackbar("Booking failed: $msg")
            }
        }
    }

    val rooms = viewModel.roomsState.collectAsState().value
    val firstRoom = rooms.firstOrNull()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(addLoading) {
        if (addLoading) {
            focusManager.clearFocus()
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .background(Color(0xFFFDE2E4))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                TitleWithBackIcon(navController, hostelId ,"")
                RoomImage(firstRoom?.photoUrl)

                Text("Check-in Date   -   Check-out Date", Modifier.padding(start = 6.dp))
                Row(Modifier.padding(4.dp)) {
                    TextField(
                        value = uiState.duration.getOrNull(0) ?: "",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFBDBDBD),
                            unfocusedContainerColor = Color(0xFFBDBDBD)
                        )
                    )
                    Text(text = " - ", modifier = Modifier.align(Alignment.CenterVertically).padding(4.dp))
                    TextField(
                        value = uiState.duration.getOrNull(1) ?: "",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFBDBDBD),
                            unfocusedContainerColor = Color(0xFFBDBDBD)
                        ),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }

                IdInformationField(
                    word = "Tenant ID",
                    title = uiState.tenantId,
                    onTitleChange = viewModel::onIdChange,
                    isError = false,
                    errorMessage = "",
                    onFocus = { }
                )

                InformationField(
                    word = "Tenant Name",
                    title = uiState.tenantName,
                    onTitleChange = viewModel::onNameChange,
                    isError = false,
                    errorMessage = "",
                    onFocus = { }
                )

                InformationField(
                    word = "Phone Number",
                    title = uiState.phoneNumber,
                    onTitleChange = viewModel::onPhoneNumberChange,
                    isError = false,
                    errorMessage = "",
                    onFocus = { }
                )

                InformationField(
                    word = "Tenant Email",
                    title = uiState.email,
                    onTitleChange = viewModel::onEmailChange,
                    isError = true,
                    errorMessage = "",
                    onFocus = { viewModel.clearError() }
                )

                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    ConfirmSample(
                        viewModel = viewModel,
                        roomId = roomId,
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        scope = scope
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

            }

            if (addLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(onClick = { /* consume clicks */ })
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Creating booking...", color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun TitleWithBackIcon(navController: NavController,hostelId:String, word : String){
    Row(modifier = Modifier
        .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ){
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Spacer(modifier = Modifier.weight(3f))
        Text(text= word,
            modifier = Modifier,
            color = Color(0xFF634035),
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(3f))
    }
}

@Composable
fun RoomImage(
    roomImage:String?
)
{
    Row(
        modifier = Modifier
            .height(120.dp) // 72dp
            .clip(RoundedCornerShape(8.dp)).padding(4.dp),
        //horizontalArrangement= Arrangement.Center,
        //verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        if (roomImage.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                model = roomImage,
                contentDescription = "Room Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun IdInformationField(
    word: String,
    title: String,
    onTitleChange: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String?,
    onFocus: () -> Unit
) {
    Box(
        modifier = Modifier.padding(4.dp)
            .background(Color(0xFFFDE2E4))
        , contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = word,
                modifier = Modifier.padding(start = 6.dp, end = 6.dp)
                //.width(75.sp)
            )
            val keyboardType = KeyboardType.Text

            TextField(
                value = title,
                onValueChange = { onTitleChange(it) },
                //readOnly = true,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth().onFocusChanged {
                    if (it.isFocused) {
                        onFocus()
                    }
                },
            )
        }
    }
    if (isError && errorMessage != null) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 6.dp, top = 2.dp)
        )
    }
    Spacer(modifier = Modifier.height(4.dp))

}

@Composable
fun InformationField(
    word: String,
    title: String,
    onTitleChange: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String?,
    onFocus: () -> Unit
) {
    Box(
        modifier = Modifier.padding(4.dp)
            .background(Color(0xFFFDE2E4))
                , contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = word,
                modifier = Modifier.padding(start = 6.dp, end = 6.dp)
                    //.width(75.sp)
            )
            val keyboardType = KeyboardType.Text

            TextField(
                value = title,
                onValueChange = { onTitleChange(it) },
                readOnly = true,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFBDBDBD),
                    unfocusedContainerColor = Color(0xFFBDBDBD)
                ),
                modifier = Modifier.fillMaxWidth().onFocusChanged {
                    if (it.isFocused) {
                        onFocus()
                    }
                },
            )
        }
    }
    if (isError && errorMessage != null) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 6.dp, top = 2.dp)
        )
    }
    Spacer(modifier = Modifier.height(4.dp))

}

@Composable
fun ConfirmSample(
    viewModel: TenantInformationViewModel,
    roomId: String,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    var showDialog by remember { mutableStateOf(false) }

    Button(onClick = { showDialog = true }, modifier = Modifier.padding(4.dp)) {
        Text("Booking")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm") },
            text = { Text("Are you sure you want to add this booking?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    val tenantName = viewModel.uiState.value.tenantName
                    val tenantId = viewModel.uiState.value.tenantId
                    if (tenantName.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please the correct Tenant ID")
                        }
                    }else if (tenantId != "T001" && tenantId != "T002" && tenantId != "T003" && tenantId != "T004" && tenantId != "T005"
                        && tenantId != "T006" && tenantId != "T007" && tenantId != "T008" && tenantId != "T009" && tenantId != "T010"){
                        scope.launch {
                            snackbarHostState.showSnackbar("Please the correct Tenant ID")
                        }
                    }
                    else {
                        viewModel.addBookingAsync(roomId)
                        scope.launch {
                            snackbarHostState.showSnackbar("Add Booking Successful")
                        }
                        navController.navigate("booking_hostel")
                    }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

