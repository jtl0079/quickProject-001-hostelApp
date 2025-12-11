package com.example.hostelmanagement.booking.bookingHostel


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hostelmanagement.HostelUiState
import com.example.hostelmanagement.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun BookingHostelScreen(navController: NavController,) {
    val itemsList = remember { mutableStateListOf<HostelUiState>() }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("Branch")
                .document(uid)
                .collection("Hostel")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("HomePage", "Listen failed.", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        itemsList.clear()
                        for (doc in snapshot.documents) {
                            val hostel = HostelUiState(
                                hostelId = doc.getString("hostelId") ?: "",
                                hostelPicture = doc.getString("hostelPicture") ?: "",
                                hostelName = doc.getString("hostelName") ?: "",
                                hostelAddress = doc.getString("hostelAddress") ?: "",
                                hostelCity = doc.getString("hostelCity") ?: "",
                                hostelState = doc.getString("hostelState") ?: "",
                                hostelPostalCode = doc.getString("hostelPostalCode") ?: ""
                            )
                            itemsList.add(hostel)
                        }
                    }
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                WindowInsets.systemBars.asPaddingValues()
            )
            .background(Color(0xFFFDE2E4))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ){
            Title("Booking Hostel")

            FloatingButtonLazyColumn(navController,itemsList,modifier = Modifier.weight(1f))
            FourButton(navController,Modifier)
        }
    }
}
@Composable
fun FourButton(navController: NavController, modifier: Modifier = Modifier){
    Row(
        modifier = modifier
        .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ){
        Button(
            onClick = {navController.navigate("home")},
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.home),
                contentDescription = "Home",
                modifier = Modifier.size(40.dp)
            )
        }

        // Booking Button
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.booking),
                contentDescription = "Booking",
                modifier = Modifier.size(40.dp)
            )
        }

        // Report Button
        Button(
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.report),
                contentDescription = "Report",
                modifier = Modifier.size(40.dp)
            )
        }

        // Profile Button
        Button(
            onClick = { navController.navigate("profile") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Profile",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
@Composable
fun FloatingButtonLazyColumn(navController: NavController, hostelRepository: List<HostelUiState>, modifier: Modifier) {

    Box(modifier = modifier.fillMaxSize()) {


        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(hostelRepository) { hostel ->
                Hostels(navController,hostel,modifier = Modifier)
            }
        }
        Button(
            onClick = { navController.navigate("booking_history") },
            modifier = Modifier
                .width(180.dp)
                .height(90.dp)
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9DFCFF),
                contentColor = Color(0xFF634035)
            )
        ) {
            Text("History")
        }
    }
}


@Composable
fun Hostels(
    navController: NavController,
    hostel: HostelUiState
    ,modifier: Modifier){
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                navController.navigate("booking_room/${hostel.hostelId}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
    ) {
        HostelPages(hostel = hostel)
    }
}

@Composable
fun HostelPages(
    hostel : HostelUiState,
    ){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(72.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HostelImage(hostel.hostelPicture)
        Spacer(modifier = Modifier.width(16.dp))
        HostelInfor(hostel.hostelName,hostel.hostelAddress,modifier = Modifier.weight(1f))
    }
}

@Composable
fun HostelInfor(
    name:String,
    description:String,
    modifier: Modifier = Modifier
){
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = name,
            //fontFamily = Cabin,
            fontWeight = FontWeight.Bold ,
            color = Color(0xFF634035),
            style = MaterialTheme.typography.displaySmall

        )
        Text(
            text = description,
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
fun HostelImage(
    hostelImage:String?,
)
{
    Box(
        modifier = Modifier
            .size(72.dp) // 72dp
            .clip(RoundedCornerShape(8.dp)) //8dp
    ) {
        if (hostelImage.isNullOrEmpty()) {
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
                model = hostelImage,
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
fun Title(word : String){
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Spacer(modifier = Modifier.weight(1f))
            Text(text= word,
                modifier = Modifier,
                color = Color(0xFF634035),
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
}
