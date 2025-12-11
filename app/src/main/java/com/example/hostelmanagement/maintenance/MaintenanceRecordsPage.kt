package com.example.hostelmanagement.maintenance


import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hostelmanagement.ui.theme.blue
import com.example.hostelmanagement.ui.theme.brown
import com.example.hostelmanagement.ui.theme.pink
import kotlinx.coroutines.launch
import kotlin.collections.find

@Composable
fun Error(
    context: Context,
    viewModel: MaintenanceViewModel
){
    val failedDelete by viewModel.failedDelete.collectAsState()
    val failedRead by viewModel.failedRead.collectAsState()
    val failedReadAll by viewModel.failedReadAll.collectAsState()

    LaunchedEffect(failedDelete) {//if got value will launch the toast
        if (failedDelete.isNotEmpty()) {
            Toast.makeText(context, failedDelete, Toast.LENGTH_SHORT).show()
            viewModel.clearDelete()
        }
    }

    LaunchedEffect(failedRead){
        if(failedRead.isNotEmpty()){
            Toast.makeText(context, failedRead, Toast.LENGTH_SHORT).show()
            viewModel.clearRead()
        }
    }

    LaunchedEffect(failedReadAll){
        if(failedReadAll.isNotEmpty()){
            Toast.makeText(context, failedReadAll, Toast.LENGTH_SHORT).show()
            viewModel.clearReadAll()
        }
    }

}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceRecordsScreen(
    maintenanceViewModel: MaintenanceViewModel,
    navController: NavController,
    onDone: () -> Unit
){

    val tabs = listOf("Active", "History")

    val pagerState = rememberPagerState(initialPage = 0){//control the app will show which page first once open (initial part)
        tabs.size// tell pager got how many page
    }

    val coroutineScope = rememberCoroutineScope()

    Error(LocalContext.current, maintenanceViewModel)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0,0,0,0)//all the part let me control
    ){paddingValues ->

        Column(
            modifier = Modifier
                .background(pink)
                .padding(paddingValues)
                .fillMaxSize()
        ){
            Row(
                modifier = Modifier
                    .background(blue)
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = { onDone()},
                )  {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                Text(
                    text = "Maintenance Records",
                    color = brown,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TabRow(//control the page
                selectedTabIndex = pagerState.currentPage, //let the tab row know which tab is selected now(will control the indicator - the line show at which tab)
                modifier = Modifier
                    .fillMaxWidth(),
                containerColor = brown
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
                                if(pagerState.currentPage == index) brown else Color.Gray
                            )
                        ,
                        selectedContentColor = White,
                        unselectedContentColor = LightGray,
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
                    0 -> ActiveHistory(maintenanceViewModel, navController)
                    1 -> History(maintenanceViewModel)
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
fun ActiveHistory(maintenanceViewModel: MaintenanceViewModel, navController: NavController){

    val roomID by maintenanceViewModel.selectedRoomId.collectAsState()


    LaunchedEffect(Unit){
        maintenanceViewModel.readMaintenanceHistory(roomID)
    }

    val maintenanceList by maintenanceViewModel.maintenanceList.collectAsState()

    val activeList = remember(maintenanceList){
        maintenanceList.filter { it.status == MaintenanceStatus.IN_PROGRESS }
    }//the state flow will let ui recompose
    //filter will do by ui
    //filter will not trigger the recompose
    //this can avoid the old record is jumping
    //so will be recompose then follow by filter

    var selectedId by remember{ mutableStateOf<String?>(null)}

    val context = LocalContext.current

    LaunchedEffect(Unit){
        maintenanceViewModel.show.collect{ message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
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
            items(activeList){ maintenance ->
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier
                        .width(370.dp)
                        .clickable{
                            selectedId = maintenance.maintenanceId // click the card to open the pop up
                        },
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 10.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ){

                    Text(
                        text = "Maintenance ID : ${maintenance.maintenanceId}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)

                    )

                    WordManage("Room ID", maintenance.roomId)

                    WordManage("Request Date ", maintenance.requestDate)

                    WordManage("Status", maintenance.status.toString())

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        UpdateButton(maintenanceViewModel, maintenance, navController)
                        CompleteButton(maintenanceViewModel, maintenance)
                    }

                    DeleteButton(maintenanceViewModel, maintenance)
                }


            }
        }
        //so the id is not null when the card is clicked
        if(selectedId != null){//to control the pop up maintenance details
            MaintenanceDetails(
                maintenanceId = selectedId!!,
                onDismiss = {selectedId = null},//close the pop up
                maintenanceList
            )
        }
    }
}



@Composable
fun History(maintenanceViewModel: MaintenanceViewModel){

    val roomID by maintenanceViewModel.selectedRoomId.collectAsState()

    LaunchedEffect(Unit){
        maintenanceViewModel.readMaintenanceHistory(roomID)
    }

    val maintenanceList by maintenanceViewModel.maintenanceList.collectAsState()

    val historyList = remember(maintenanceList){
        maintenanceList.filter { it.status != MaintenanceStatus.IN_PROGRESS }
    }

    //val maintenanceList by maintenanceViewModel.maintenanceList.collectAsState()
    var selectedId by remember{ mutableStateOf<String?>(null)}

    Box(//Big Box to show all the active maintenance request list
        modifier = Modifier
            .fillMaxSize()

    ){
        LazyColumn(//load all the maintenance request list
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            items(historyList){ maintenance ->
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier
                        .width(370.dp)
                        .clickable{
                            selectedId = maintenance.maintenanceId // click the card to open the pop up
                        },
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 10.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ){

                    Text(
                        text = "Maintenance ID : ${maintenance.maintenanceId}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)

                    )

                    WordManage("Room ID", maintenance.roomId)

                    WordManage("Request Date ", maintenance.requestDate)

                    WordManage("Status", maintenance.status.toString())

                    Spacer(modifier = Modifier.height(16.dp))


                }

            }
        }
        //so the id is not null when the card is clicked
        if(selectedId != null){//to control the pop up maintenance details
            MaintenanceDetails(
                maintenanceId = selectedId!!,
                onDismiss = {selectedId = null},//close the pop up
                maintenanceList
            )
        }
    }
}


@Composable
fun MaintenanceDetails(
    maintenanceId: String,
    onDismiss: () -> Unit,
    maintenanceList: List<Maintenance>
){
    val maintenanceList = maintenanceList

    val maintenanceDetails = maintenanceList.find{it.maintenanceId == maintenanceId}
    var show by remember{ mutableStateOf(false)}

    LaunchedEffect(Unit){
        show = true
    }
    //The main thread renders the first frame with show = false
    //Then the coroutine sets show = true
    //In the next frame, the main thread sees show = true and starts the animation

    val alpha by animateFloatAsState(//control the transparency of the pop up
        targetValue = if(maintenanceDetails != null && show) 1f else 0f,//if maintenance details are not null will be show
        animationSpec = tween(500)//in the 500ms will show the pop up
    )
    //animateFloatAsState = calculates animation values per frame
    //animateFloatAsState calculates the current animation value, and compose uses that value to redraw the Ui for each frame

    val scale by animateFloatAsState(//control the size of the pop up
        targetValue = if(maintenanceDetails != null && show) 1f else 0.5f,//if the maintenance details is not null will be show the target size
        animationSpec = tween(500)//in the 500ms will from small to big
    )
    Box(//to cover the Big Box and show the pop up so the color is black transparent (dim background)
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f* alpha))//follow the animation become dim background
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
                .size(360.dp, 600.dp)
                .background(White)
                .border(2.dp, Color.Black)
                .clickable(enabled = false){},
            contentAlignment = Alignment.TopCenter
        ){
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "Maintenance ID : ${maintenanceDetails?.maintenanceId}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 16.dp)

                )

                Divider(Modifier.padding(vertical = 2.dp))

                WordManage("Room ID", maintenanceDetails?.roomId?: "")

                WordManage("Request Date ", maintenanceDetails?.requestDate?: "")

                WordManage("Status", maintenanceDetails?.status.toString())

                if(maintenanceDetails?.status == MaintenanceStatus.COMPLETED){
                    WordManage("Resolve Date", maintenanceDetails.resolveDate)
                }

                Card(
                    modifier = Modifier
                        .width(350.dp)
                        .padding(top = 10.dp)
                        .border(2.dp, LightGray,RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = LightGray
                    ),
                    shape = RoundedCornerShape(16.dp)
                ){
                    maintenanceDetails?.item?.forEach{ item ->

                        WordManage("Category", item.category)

                        WordManage("Item Name", item.item)

                        WordManage("Issue", item.issue)


                        Divider(Modifier.padding(vertical = 2.dp))

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Card(
                    modifier = Modifier
                        .width(350.dp)
                        .padding(top = 10.dp, bottom = 20.dp)
                        .border(2.dp, LightGray, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = LightGray
                    ),
                    shape = RoundedCornerShape(16.dp)
                ){
                    Text(
                        text ="Remarks",
                        modifier = Modifier
                            .padding(start = 16.dp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = maintenanceDetails?.remarks ?: "",
                        modifier = Modifier
                            .padding(start = 16.dp),
                        fontSize = 16.sp
                    )
                }


            }
        }
    }

}

@Composable
fun WordManage(field: String, value: String){
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ){
        Row(
            modifier = Modifier
                .width(200.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Start
        ){
            Text(
                text = field,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .width(300.dp),
            horizontalArrangement = Arrangement.Start
        ){
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun UpdateButton(maintenanceViewModel: MaintenanceViewModel, maintenance: Maintenance, navController: NavController){
    Column(
        modifier = Modifier
    ){
        Button(
            onClick = {
                maintenanceViewModel.edit(maintenance)
                navController.navigate("maintenanceRequest")
            },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = brown,
                contentColor = White
            ),
            modifier = Modifier
                .padding(5.dp)

        ){
            Text(
                text = "Update",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CompleteButton(maintenanceViewModel: MaintenanceViewModel, maintenance: Maintenance){

    Column(
        modifier = Modifier
    ){
        Button(
            onClick = {
                maintenanceViewModel.updateMaintenanceStatusResolved(
                    maintenance)
            },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = brown,
                contentColor = White
            ),
            modifier = Modifier
                .padding(5.dp)

        ){
            Text(
                text = "Complete",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



@Composable
fun DeleteButton(maintenanceViewModel: MaintenanceViewModel, maintenance: Maintenance){

    IconButton(
        onClick = {
            maintenanceViewModel.deleteMaintenance(
                maintenance
            )
        }
    ){
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = Color.Black,
            modifier  = Modifier.size(28.dp)
        )
    }
}
