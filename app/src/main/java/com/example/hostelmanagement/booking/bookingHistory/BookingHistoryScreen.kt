package com.example.hostelmanagement.booking.bookingHistory

import android.R.attr.fontWeight
import android.graphics.Color.red
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hostelmanagement.booking.bookingHistory.BookingHistoryViewModel
import com.example.hostelmanagement.booking.booking.BookingEntity
import com.example.hostelmanagement.booking.tenantInformation.TenantEntity
import com.example.hostelmanagement.booking.tenantInformation.TenantInformationViewModel
import com.example.hostelmanagement.maintenance.MaintenanceDetails
import com.example.hostelmanagement.maintenance.WordManage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(navController: NavController,bookingHistoryViewModel: BookingHistoryViewModel){

    val tabs = listOf("Complete", "Cancel")

    val pagerState = rememberPagerState(initialPage = 0){//control the app will show which page first once open (initial part)
        tabs.size// tell pager got how many page
    }

    val coroutineScope = rememberCoroutineScope()
    val uiState by bookingHistoryViewModel.uiState.collectAsState()
    val completeList = uiState.completeBooked
    val cancelList = uiState.cancelBooked
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.fillMaxSize().padding(WindowInsets.systemBars.asPaddingValues()),
        contentWindowInsets = WindowInsets(0,0,0,0)//all the part let me control
    ){paddingValues ->

        Column(
            modifier = Modifier
                .background(Color(0xFFFDE2E4))
                .padding(paddingValues)
                .fillMaxSize()
        ){
            Row(
                modifier = Modifier
                    .background(Color(0xFFCCF1F2))
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigate("booking_hostel") }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.weight(2f))
                Text(
                    text = "Booking History",
                    color = Color(0xFF634035),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(5f))
            }

            TabRow(//control the page
                selectedTabIndex = pagerState.currentPage, //let the tab row know which tab is selected now(will control the indicator - the line show at which tab)
                modifier = Modifier
                    .fillMaxWidth(),
                containerColor = Color(0xFF634035)
            ){
                tabs.forEachIndexed{ index, title->
                    Tab(
                        selected = pagerState.currentPage == index,//is to remind the tab at which page is selected (for control which tab should be light)
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)//it is a suspend function (the system will help u to change to a new page)
                            }//after the animation, HorizontalPager will know which page you want to go

                        },
                        modifier = Modifier
                            .background(
                                if(pagerState.currentPage == index) Color(0xFF634035) else Color.Gray
                            )
                        ,
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.LightGray,
                        text = {
                            Text(
                                text = title,
                                fontSize = 16.sp
                            )
                        }

                    )
                }
            }

            HorizontalPager(//will change the page number and control to show which page
                state = pagerState,//tell the pager which page is selected
                modifier = Modifier.fillMaxSize()
            ){page ->
                when(page){
                    0 -> History(0,navController,bookingHistoryViewModel,completeList,snackbarHostState, scope)
                    1 -> History(1,navController,bookingHistoryViewModel,cancelList,snackbarHostState,scope)
                }

            }

            //1) press tab
            //- tab change the pagerState.currentPage
            //- horizontalPager know the new index so automatically change to new page
            //- tab row see the index is changed will change the indicator

            //2) scroll page
            // - pagerState.currentPage change
            // - tab row see the index is changed will change the indicator

        }

    }
}

@Composable
fun History(selection:Int,navController: NavController, bookingHistoryViewModel: BookingHistoryViewModel,
            bookingList:List<BookingEntity>,snackbarHostState: SnackbarHostState,
            scope: CoroutineScope
){
    var selectedId by remember{ mutableStateOf<String?>(null)}
    //val maintenanceList by bookingHistoryViewModel..collectAsState()
    Box(//Big Box to show all the active maintenance request list
        modifier = Modifier
            .fillMaxSize()

    ){
        LazyColumn(//load all the maintenance request list
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            items(bookingList){ booking ->
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier
                        .width(370.dp)
                        .clickable{
                            selectedId = booking.bookingId // click the card to open the pop up
                        },
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 10.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFCCF1F2)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ){

                    Text(
                        text = "Booking ID : ${booking.bookingId}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)

                    )
                    CardInformation("Room ID", booking.roomId)
                    CardInformation("Tenant ID",booking.tenantId)
                    CardInformation("Date Booking", booking.bookingDate)
                    CardInformation("Check-In Date", booking.bookedStartDate)
                    CardInformation("Check-Out Date", booking.bookedEndDate)
                    CardInformation("Status", booking.bookingStatus)
                    Spacer(modifier = Modifier.height(4.dp))

                }


            }
        }
        if(selectedId != null){//to control the pop up maintenance details
            if(selection == 0){
                BookingDetails(
                    bookingId = selectedId!!,
                    onDismiss = {selectedId = null},//close the pop up
                    bookingList = bookingList,
                    bookingHistoryViewModel,
                    snackbarHostState,
                    scope
                )
            }
            else{
                CancelDetails(
                    bookingId = selectedId!!,
                    onDismiss = {selectedId = null},//close the pop up
                    bookingList = bookingList,
                    bookingHistoryViewModel,
                    snackbarHostState,
                    scope
                )
            }
        }
    }
}

@Composable
fun CancelDetails(
    bookingId: String,
    onDismiss: () -> Unit,
    bookingList: List<BookingEntity>,
    viewModel: BookingHistoryViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
){
    val bookingList = bookingList
    val tenantViewModel : TenantInformationViewModel = viewModel()
    val allTenantRecord = tenantViewModel.tenant
    var tenantRecord: TenantEntity = TenantEntity()
    val bookingDetails = bookingList.find({it.bookingId == bookingId})
    var show by remember{ mutableStateOf(false)}

    LaunchedEffect(Unit){
        show = true
        if (bookingDetails != null && !bookingDetails.roomId.isNullOrBlank()) {
            viewModel.fetchRoomInfo(bookingDetails.roomId)
        }
    }
    val uiState by viewModel.uiState.collectAsState()
    val room = uiState.selectedRoom
    val isLoading by viewModel.loading.collectAsState(initial = false)
    Log.d("RoomDebug", "error space: ${room?.roomPrice}")
    val alpha by animateFloatAsState(//control the transparency of the pop up
        targetValue = if(bookingDetails != null && show) 1f else 0f,//if maintenance details are not null will be show
        animationSpec = tween(500)//in the 200ms will show the pop up
    )
    //animateFloatAsState = calculates animation values per frame
    //animateFloatAsState calculates the current animation value, and compose uses that value to redraw the Ui for each frame

    val scale by animateFloatAsState(//control the size of the pop up
        targetValue = if(bookingDetails != null && show) 1f else 0.5f,//if the maintenance details is not null will be show the target size
        animationSpec = tween(500)//in the 200ms will from small to big
    )
    for(tenant in allTenantRecord){
        if(tenant.tenantId == bookingDetails?.tenantId){
            tenantRecord = tenant
        }
    }
    Box(//to cover the Big Box and show the pop up so the color is black transparent (dim background)
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f* alpha))//follow the animation become dim background
            .clickable(
                indication = null,
                interactionSource = remember {MutableInteractionSource()}
            ){
                onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(//this is the pop up that show the maintenance details
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
                .size(360.dp, 500.dp)
                .background(Color.White)
                .border(2.dp, Color.Black)
                .clickable(enabled = false){},
            contentAlignment = Alignment.TopCenter
        ){
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "Booking ID : ${bookingDetails?.bookingId}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp)

                )
                CardInformation("Tenant ID", bookingDetails?.tenantId?:"")

                CardInformation("Name", tenantRecord.tenantName)

                CardInformation("Email", tenantRecord.tenantEmail)

                CardInformation("Phone Number", tenantRecord.tenantPhoneNumber)
                Divider(Modifier.padding(vertical = 2.dp))

                //Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                    ,
                    colors = CardDefaults.cardColors(
                        containerColor =Color(0xFFCCF1F2)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ){
                    CardInformation("Room ID", bookingDetails?.roomId?: "")

                    val typeDisplay = when {
                        isLoading -> "Loading..."
                        room?.roomType != null -> {
                            String.format(room.roomType)
                        }
                        else -> "N/A"
                    }
                    CardInformation("Type", typeDisplay)

                    val capacityDisplay = when {
                        isLoading -> "Loading..."
                        room?.roomCapacity != null -> {
                            String.format("${room.roomCapacity} Person")
                        }
                        else -> "N/A"
                    }
                    CardInformation("Capacity", capacityDisplay)

                    val priceDisplay = when {
                        isLoading -> "Loading..."
                        room?.roomPrice != null -> {
                            String.format("RM %.2f", room.roomPrice)
                        }
                        else -> "N/A"
                    }
                    CardInformation("Price", priceDisplay)

                    val hostelDisplay = when {
                        isLoading -> "Loading..."
                        room?.roomPrice != null -> {
                            String.format(room.hostelId?:"N/A")
                        }
                        else -> "N/A"
                    }
                    CardInformation("Hostel ID", hostelDisplay)


                }

            }
        }

    }

}

@Composable
fun BookingDetails(
    bookingId: String,
    onDismiss: () -> Unit,
    bookingList: List<BookingEntity>,
    viewModel: BookingHistoryViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
){
    val bookingList = bookingList
    val tenantViewModel : TenantInformationViewModel = viewModel()
    val allTenantRecord = tenantViewModel.tenant
    var tenantRecord: TenantEntity = TenantEntity()
    val bookingDetails = bookingList.find({it.bookingId == bookingId})
    var show by remember{ mutableStateOf(false)}

    LaunchedEffect(Unit){
        show = true
        if (bookingDetails != null && !bookingDetails.roomId.isNullOrBlank()) {
            viewModel.fetchRoomInfo(bookingDetails.roomId)
        }
    }
    val uiState by viewModel.uiState.collectAsState()
    val room = uiState.selectedRoom
    val isLoading by viewModel.loading.collectAsState(initial = false)
    Log.d("RoomDebug", "error space: ${room?.roomPrice}")
    val alpha by animateFloatAsState(//control the transparency of the pop up
        targetValue = if(bookingDetails != null && show) 1f else 0f,//if maintenance details are not null will be show
        animationSpec = tween(500)//in the 200ms will show the pop up
    )
    //animateFloatAsState = calculates animation values per frame
    //animateFloatAsState calculates the current animation value, and compose uses that value to redraw the Ui for each frame

    val scale by animateFloatAsState(//control the size of the pop up
        targetValue = if(bookingDetails != null && show) 1f else 0.5f,//if the maintenance details is not null will be show the target size
        animationSpec = tween(500)//in the 200ms will from small to big
    )
    for(tenant in allTenantRecord){
        if(tenant.tenantId == bookingDetails?.tenantId){
            tenantRecord = tenant
        }
    }
    Box(//to cover the Big Box and show the pop up so the color is black transparent (dim background)
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f* alpha))//follow the animation become dim background
            .clickable(
                indication = null,
                interactionSource = remember {MutableInteractionSource()}
            ){
                onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(//this is the pop up that show the maintenance details
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
                .size(360.dp, 500.dp)
                .background(Color.White)
                .border(2.dp, Color.Black)
                .clickable(enabled = false){},
            contentAlignment = Alignment.TopCenter
        ){
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "Booking ID : ${bookingDetails?.bookingId}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp)

                )
                CardInformation("Tenant ID", bookingDetails?.tenantId?:"")

                CardInformation("Name", tenantRecord.tenantName)

                CardInformation("Email", tenantRecord.tenantEmail)

                CardInformation("Phone Number", tenantRecord.tenantPhoneNumber)
                Divider(Modifier.padding(vertical = 2.dp))

                //Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        ,
                    colors = CardDefaults.cardColors(
                        containerColor =Color(0xFFCCF1F2)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ){
                    CardInformation("Room ID", bookingDetails?.roomId?: "")

                    val typeDisplay = when {
                        isLoading -> "Loading..."
                        room?.roomType != null -> {
                            String.format(room.roomType)
                        }
                        else -> "N/A"
                    }
                    CardInformation("Type", typeDisplay)

                    val capacityDisplay = when {
                        isLoading -> "Loading..."
                        room?.roomCapacity != null -> {
                            String.format("${room.roomCapacity} Person")
                        }
                        else -> "N/A"
                    }
                    CardInformation("Capacity", capacityDisplay)

                    val priceDisplay = when {
                    isLoading -> "Loading..."
                    room?.roomPrice != null -> {
                        String.format("RM %.2f", room.roomPrice)
                    }
                    else -> "N/A"
                }
                    CardInformation("Price", priceDisplay)

                    val hostelDisplay = when {
                    isLoading -> "Loading..."
                    room?.roomPrice != null -> {
                        String.format(room.hostelId?:"N/A")
                    }
                    else -> "N/A"
                }
                    CardInformation("Hostel ID", hostelDisplay)


                }

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically)
                {
                    ConfirmSample(viewModel, bookingDetails?.bookingId ?: "",snackbarHostState,scope)
                    //snackbar
                }
            }
        }

    }

}

@Composable
fun ConfirmSample(viewModel: BookingHistoryViewModel,bookingId:String,
                  snackbarHostState: SnackbarHostState,
                  scope: CoroutineScope) {
    var showDialog by remember { mutableStateOf(false) }

    Button(onClick = { showDialog = true },Modifier.padding(4.dp)) {
        Text("Cancel Booking")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm") },
            text = { Text("Are you sure you want to Delete this booking?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateBookingStatusToCancel(bookingId)
                    showDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Cancel Booking Successful")
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


@Composable
fun <T>CardInformation(text:String,data:T) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .width(170.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 0.dp, end = 0.dp)
            )
        }

        Row(
            modifier = Modifier,
                //.width(300.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = " : $data",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 0.dp, end = 8.dp)
            )
        }
    }
}


