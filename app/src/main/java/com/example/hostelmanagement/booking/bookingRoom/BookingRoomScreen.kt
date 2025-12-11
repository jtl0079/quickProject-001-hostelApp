package com.example.hostelmanagement.booking.bookingRoom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hostelmanagement.RoomItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun BookingRoomScreen(id:String,navController: NavController,
    viewModel: BookingRoomViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(id) {
        viewModel.observeRooms(id)
    }
    val rooms by viewModel.roomsState.collectAsState()
    val context = LocalContext.current
    val imageLoader=remember{ImageLoader.Builder(context).build()}
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(rooms) {
        rooms.forEach { room->
            room.photoUrl?.let{url->
                val req= ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .build()
                imageLoader.enqueue(req)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                WindowInsets.systemBars.asPaddingValues()
            ).background(Color(0xFFFDE2E4))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ){
            TitleWithBackIcon(navController,viewModel,"Room")
            FloatingButtonLazyColumn(id,navController,viewModel,uiState,modifier = Modifier.weight(1f))
        }
        if(uiState.filter == false){
            Filter(navController,viewModel,uiState, snackbarHostState, scope)

        }
    }
}

@Composable
fun FloatingButtonLazyColumn(hostelId:String,navController: NavController,viewModel: BookingRoomViewModel, uiState: BookingRoomUiState, modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(uiState.validRoom) { room ->
                Rooms(navController,hostelId,room,uiState,modifier = Modifier)
            }
        }
    }
}

@Composable
fun Rooms(
    navController: NavController,
    hostelId : String,
    room: RoomItem,
    uiState: BookingRoomUiState,
    modifier: Modifier){
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp).
            clickable {
                navController.navigate("tenant_Information/${hostelId}/${room.roomId}/${uiState.sem}/${uiState.durationYear}/${uiState.durationMonth}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
    ) {
        RoomPages(room = room)
    }
}

@Composable
fun RoomPages(
    room : RoomItem){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(72.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        RoomImage(room.photoUrl)
        Spacer(modifier = Modifier.width(16.dp))
        RoomInformation(room.roomId,room.roomDescription,modifier = Modifier.weight(1f))
    }
}

@Composable
fun RoomInformation(
    name:String?,
    description:String?,
    modifier: Modifier = Modifier
){
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = name ?:"",
            //fontFamily = Cabin,
            fontWeight = FontWeight.Bold ,
            color = Color(0xFF634035),
            style = MaterialTheme.typography.displaySmall

        )
        Text(
            text = description ?:"",
            //fontFamily = Cabin,
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF634035),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,

            )
    }
}

@Composable
fun RoomImage(
    roomImage:String?
)
{
    Box(
        modifier = Modifier
            .size(72.dp) // 72dp
            .clip(RoundedCornerShape(8.dp)) //8dp
    ) {
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
    }
}
@Composable
fun TitleWithBackIcon(navController: NavController, bookingRoomViewModel: BookingRoomViewModel, word : String){
    Spacer(modifier = Modifier.height(16.dp))
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
        IconButton(
            onClick = {bookingRoomViewModel.changeFilterState(false)},
            modifier = Modifier
        ) {
            Icon(
                imageVector = Icons.Filled.FilterList,
                contentDescription = "Filter",
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun Filter(
    navController: NavController,
    bookingRoomViewModel: BookingRoomViewModel,
    uiState: BookingRoomUiState,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
){
    val sem = listOf("January","June","October")
    val durationMonth= listOf("None","1 Month","2 Month","3 Month","4 Month","5 Month","6 Month",
        "7 Month","8 Month","9 Month","10 Month","11 Month","12 Month")
    val durationYear = listOf("None"," 1 Year "," 2 Year "," 3 Year ")
    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)).padding(start = 30.dp,top = 60.dp,end = 30.dp,bottom = 60.dp)
            .pointerInput(Unit) {
            detectTapGestures(onTap = { /* consumed */ })
        },
        //contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier
                .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ){
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Please fill in the information")
            Spacer(modifier = Modifier.height(16.dp))
            SimpleDropdown(sem,"Sem Check-in",uiState.sem,
                onTextChange = { bookingRoomViewModel.updateSem(it) })

            Spacer(modifier = Modifier.height(16.dp))
            SimpleDropdown(durationYear,"Duration of Year",uiState.durationYear,
                onTextChange = { bookingRoomViewModel.updateDurationYear(it) })

            Spacer(modifier = Modifier.height(16.dp))
            SimpleDropdown(durationMonth,"Duration of Month",uiState.durationMonth,
                onTextChange = { bookingRoomViewModel.updateDurationMonth(it) })

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if((uiState.durationYear =="None" && uiState.durationMonth =="None")||
                        (uiState.durationYear =="" && uiState.durationMonth =="None")||
                        (uiState.durationYear =="None" && uiState.durationMonth =="")||
                        (uiState.durationYear =="" && uiState.durationMonth =="")
                        ){
                        scope.launch {
                            snackbarHostState.showSnackbar("Please add the duration")
                        }
                    }else{
                        bookingRoomViewModel.onFilterButtonClicked(uiState.sem,uiState.durationYear,uiState.durationMonth)

                    }
                          },
                modifier = Modifier
                    .width(120.dp)
                    .height(50.dp)
                ) {
                Text(text="Submit")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdown(
    items: List<String>,question:String,text:String,
    onTextChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = text,
            onValueChange = {},
            readOnly = true,
            label = { Text(question) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onTextChange(item)
                        expanded = false
                    }
                )
            }
        }
    }
}


