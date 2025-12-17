package com.example.hostelmanagement.maintenance

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.hostelmanagement.ui.theme.blue
import com.example.hostelmanagement.ui.theme.brown
import com.example.hostelmanagement.ui.theme.dim
import com.example.hostelmanagement.ui.theme.green
import com.example.hostelmanagement.ui.theme.lightGreen
import com.example.hostelmanagement.ui.theme.pink


@Composable
fun DisplayEmptyMessage(context: Context, viewModel: MaintenanceViewModel){

    val currentContext by rememberUpdatedState(context)
    val showEmptyMessage by viewModel.showEmptyMessage.collectAsState()

    LaunchedEffect(showEmptyMessage){
        if(showEmptyMessage){
            Toast.makeText(currentContext, "Please select maintenance items to proceed.", Toast.LENGTH_SHORT).show()
            viewModel.closeEmptyMessage()
        }
    }
}

@Composable
fun DisplayEmptyDialog(context: Context, viewModel: ItemViewModel){

    val currentContext by rememberUpdatedState(context)
    val issueMessage by viewModel.issueMessage.collectAsState()

    LaunchedEffect(issueMessage){
        if(issueMessage){
            Toast.makeText(currentContext, "Please select maintenance items or write remarks to proceed.", Toast.LENGTH_SHORT).show()
            viewModel.closeIssueMessage()
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceRequestScreen(
    itemViewModel: ItemViewModel,
    maintenanceViewModel: MaintenanceViewModel,
    onDone: () -> Unit
){

    val roomItems by itemViewModel.roomItems.collectAsState()
    val bathroomItems by itemViewModel.bathroomItems.collectAsState()
    val roomEquipment = "Room Equipment"
    val room = "Room"
    val bathroomEquipment = "Bathroom Equipment"
    val bathroom = "Bathroom"
    val showDialog by itemViewModel.showDialog.collectAsState()
    val open by itemViewModel.showDialog.collectAsState()
    val editMaintenance by maintenanceViewModel.editMaintenance.collectAsState()
    val roomId by maintenanceViewModel.selectedRoomId.collectAsState()

    LaunchedEffect(editMaintenance){

        val mr = editMaintenance?: return@LaunchedEffect

        maintenanceViewModel.remarks.value = mr.remarks

        itemViewModel.load(mr.item)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        contentWindowInsets = WindowInsets(0,0,0,0)

    ){paddingValues ->
        Column(
            modifier = Modifier
                .background(pink)
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    text = "Maintenance Request",
                    color = brown,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .background(brown)
                    .height(50.dp)
                    .width(350.dp)
                    .border(3.dp, Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center

            ){
                Text(
                    text = "Room ID: $roomId",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(brown)
                        .padding(4.dp)

                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            RoomAndBathroomItems(
                itemViewModel = itemViewModel,
                items = roomItems,
                name1 = roomEquipment,
                name2 = room
            )

            Spacer(modifier = Modifier.height(30.dp))

            RoomAndBathroomItems(
                itemViewModel = itemViewModel,
                items = bathroomItems,
                name1 = bathroomEquipment,
                name2 = bathroom
            )

            if(showDialog){
                Dialog(
                    id = itemViewModel.selectedItem.collectAsState().value?.itemId ?: 0,
                    show = open,
                    onDismiss = {itemViewModel.closeDialog()},
                    onConfirm = { details ->
                        itemViewModel.onConfirm(details)

                    },
                    itemViewModel = itemViewModel
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Remarks(maintenanceViewModel)

            Spacer(modifier = Modifier.height(30.dp))

            SubmitOrUpdateButton(maintenanceViewModel, itemViewModel)

        }

    }

    if (maintenanceViewModel.isSubmitted.collectAsState().value) {
        ShowMaintenanceRequest(
            itemViewModel,
            maintenanceViewModel,
            onDismiss = { maintenanceViewModel.resetSubmitValue() })

    }
}

@Composable
fun Remarks(maintenanceViewModel: MaintenanceViewModel) {

    val remarks by maintenanceViewModel.remarks.collectAsState()

    TextField(
        value = remarks,
        onValueChange = { maintenanceViewModel.remarks.value = it},
        label = {
            Text("Remarks",
                color = brown,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(blue)
                    .padding(4.dp))
        },
        modifier = Modifier
            .height(200.dp)
            .width(350.dp)
            .border(2.dp, brown),
        maxLines = 10,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = blue,//when click on the text field
            unfocusedBorderColor = blue,//when not click on the text field
            focusedContainerColor = blue,
            unfocusedContainerColor = blue,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black

        ),
        textStyle = TextStyle(
            color = Color.Black,//the text key in by user
            fontSize = 16.sp
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomAndBathroomItems(
    itemViewModel: ItemViewModel,
    items: List<Item>,
    name1: String,
    name2: String
) {
    var expanded by remember { mutableStateOf(false) }
    val maintenanceIds by itemViewModel.maintenanceIds.collectAsState()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .width(350.dp)
            .background(blue)//need to change when combine
            .border(
                width = 2.dp,
                color = brown,
                shape = RoundedCornerShape(16.dp),
            )
    ) {
        OutlinedTextField(
            value = name1,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 20.sp,
                color = brown,
                fontWeight = FontWeight.SemiBold

            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = blue,
                unfocusedBorderColor = blue
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
        ) {
            items.forEach { item ->
                val isSelected = maintenanceIds.contains(item.itemId)  //when the item is loop one by one,
                // check if its id is in the selected ids when click the confirm button in dialog

                DropdownMenuItem(
                    onClick = {
                        itemViewModel.onItemClick(item, name2)//click to open the dialog
                        expanded = false
                    },
                    text = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) lightGreen else Color.White
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(
                                text = item.name,
                                fontSize = 16.sp,
                                color = if (isSelected) green else Color.Black,
                                modifier = Modifier.weight(1f)
                            )

                            if(isSelected){
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = green
                                )
                            }
                        }

                    }
                )
            }

        }
    }

}

@Composable
fun Dialog(
    id: Int,
    show : Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    itemViewModel: ItemViewModel
){
    if(show){

        val input by itemViewModel.currentIssue.collectAsState()//take the issue from the viewModel

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text ("Please enter details:")},
            text = {

                Column{

                    TextField(
                        value = input,
                        onValueChange = { itemViewModel.currentIssue.value = it },
                        label = { Text("Details") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {itemViewModel.onDelete(id)}
                    ){
                        Text("Delete", color = Color.Red)
                    }

                }
            },
            confirmButton = {
                TextButton(
                    onClick = {onConfirm(input)}
                ){
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ){
                    Text("Cancel")
                }
            }

        )

        DisplayEmptyDialog(LocalContext.current, itemViewModel)
    }
}

@Composable
fun SubmitOrUpdateButton(maintenanceViewModel: MaintenanceViewModel, itemViewModel: ItemViewModel) {

    val show by maintenanceViewModel.showEmptyMessage.collectAsState()
    val editMaintenance by maintenanceViewModel.editMaintenance.collectAsState()
    val roomId by maintenanceViewModel.selectedRoomId.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit){
        maintenanceViewModel.show.collect{ message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Button(
            onClick = {
                if(editMaintenance != null){
                    val maintenanceItems = itemViewModel.saveMaintenanceItems()
                    maintenanceViewModel.updateMaintenanceItem(
                        editMaintenance!!,
                        maintenanceItems,
                        maintenanceViewModel.remarks.value
                    )

                }else{
                    val maintenanceItems = itemViewModel.saveMaintenanceItems()
                    maintenanceViewModel.createMaintenance(
                        roomId,
                        maintenanceItems
                    )

                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = brown,
                contentColor = Color.White
            ),
            modifier = Modifier
                .align(Alignment.End)
                .padding(20.dp)
        ) {
            Text(
                text = if(editMaintenance != null) "Update" else "Submit",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if(show){
            DisplayEmptyMessage(LocalContext.current, maintenanceViewModel)
        }
    }
}

@Composable
fun ShowMaintenanceRequest(
    itemViewModel: ItemViewModel,
    maintenanceViewModel: MaintenanceViewModel,
    onDismiss: ()-> Unit
){

    val newMaintenance by maintenanceViewModel.maintenanceDetails.collectAsState()
    val id = newMaintenance?.maintenanceId ?: ""

    LaunchedEffect(Unit){
        maintenanceViewModel.readMaintenance(id)
    }

    val maintenance by maintenanceViewModel.maintenanceDetails.collectAsState()

    Error(LocalContext.current, maintenanceViewModel)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dim),
        contentAlignment = Alignment.Center
    ){
        AnimatedVisibility(//control the card to come in to the screen and show to user
            visible = true,
            enter = fadeIn() + scaleIn()
        ){
            Card(
                modifier = Modifier
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = blue
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )

            ){

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally

                ){
                    LottieCheckAnimation()
                    Text(
                        text = "Maintenance Request",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                    )

                    WordManage("Maintenance ID", maintenance?.maintenanceId?:"")
                    WordManage("Room ID", maintenance?.roomId ?: "")
                    WordManage("Request Date", maintenance?.requestDate ?: "")
                    WordManage("Status", maintenance?.status.toString())

                    Spacer(modifier = Modifier.height(20.dp))

                    maintenance?.item?.forEach{ item ->

                        WordManage("Category", item.category)

                        WordManage("Item Name", item.item)

                        WordManage("Issue", item.issue)


                        Divider(Modifier.padding(vertical = 2.dp))

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Spacer(modifier = Modifier.height(20.dp))


                    Column(
                        modifier = Modifier
                            .align(Alignment.Start)
                    ){
                        Text(
                            text ="Remarks",
                            modifier = Modifier
                                .padding(start = 16.dp),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,

                            )

                        Text(
                            text = maintenance?.remarks ?: "",
                            modifier = Modifier
                                .padding(start = 16.dp),
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            itemViewModel.resetAll()
                            maintenanceViewModel.clearAll()
                            onDismiss()},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = brown,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.End)

                    ){
                        Text(
                            text = "Close",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }

            }
        }
    }
}

@Composable
fun LottieCheckAnimation(){

    val composition by rememberLottieComposition(//read the animation
        LottieCompositionSpec.Asset("Success.json")
    )

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = 3//the animation will run 3 times
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier
            .size(120.dp)

    )
}