package com.example.hostelmanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import kotlin.collections.forEach

@Composable

fun RoomListPage(
    hostelId:String,
    onBack:()->Unit,
    onAddRoom:()->Unit,
    onRoomSelected:(RoomItem)->Unit,
    viewModel: RoomViewModel= viewModel()
){
    LaunchedEffect(hostelId) {
        viewModel.observeRooms(hostelId)
    }
    val rooms by viewModel.roomsState.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val scope= rememberCoroutineScope()
    val snackbarHostState= remember{ SnackbarHostState()}

    val context = LocalContext.current
    val imageLoader=remember{ImageLoader.Builder(context).build()}
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

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFFDE2E4))
        .padding(top=20.dp))
    {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            IconButton(onClick = { onBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Box(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Rooms",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFF634035)
                )
            }
            IconButton(onClick = { onAddRoom() }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Room"
                )
            }
        }
        if(loading){
            Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                CircularProgressIndicator()
            }
            return@Column
        }
        if(error!=null){
            Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                Text(text="Error: $error")
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(rooms){ room->
                RoomCard(room=room, onClick = {onRoomSelected(room)},
                    onDelete={clicked->
                        viewModel.deleteRoom(
                            hostelId=hostelId,
                            roomId = clicked.roomId,
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Room deleted")
                                }
                            },
                            onError = { errMsg ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error deleting room: $errMsg")
                                }
                            }
                        )
                    }
                )
            }
        }
    }
    Box(modifier=Modifier.fillMaxSize()){
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}