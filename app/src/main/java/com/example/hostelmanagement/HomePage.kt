package com.example.hostelmanagement

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.hostelmanagement.maintenance.MaintenanceViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomePage(
    onHome: () -> Unit,
    onAddItem: () -> Unit,
    onBooking: () -> Unit,
    onReport: () -> Unit,
    onProfile: () -> Unit,
    onOpenRooms: (String) -> Unit,
    maintenanceViewModel: MaintenanceViewModel
) {
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

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(WindowInsets.systemBars.asPaddingValues()),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)//all the part let me control
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDE2E4))
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFDE2E4)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Button(
                        onClick = onAddItem,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.add),
                            contentDescription = "Add",
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f),
                    ) {
                        Text(
                            text = "Hostel",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF634035),
                            fontSize = 45.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 50.dp, end = 16.dp, start = 16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(itemsList) { hostel ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .background(Color(0xFFFDE2E4))
                                .clickable {
                                    Log.d("HomePage", "Clicked on $hostel.name")
                                    maintenanceViewModel.selectedUId.value = uid ?: ""
                                    maintenanceViewModel.selectedHostelId.value = hostel.hostelId
                                    onOpenRooms(hostel.hostelId)
                                },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFFFFFFF))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (hostel.hostelPicture.isNotEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(hostel.hostelPicture),
                                        contentDescription = "Hostel Picture",
                                        modifier = Modifier
                                            .size(100.dp)
                                            .padding(end = 12.dp)
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFFFFFFF))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = hostel.hostelName,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF634035),
                                        fontSize = 25.sp
                                    )
                                    Text(
                                        text = hostel.hostelAddress,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF634035)
                                    )
                                    Text(
                                        text = "${hostel.hostelPostalCode}, ${hostel.hostelCity},",
                                        color = Color(0xFF634035)
                                    )
                                    Text(
                                        text = hostel.hostelState,
                                        color = Color(0xFF634035)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Home Button
                Button(
                    onClick = onHome,
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
                    onClick = onBooking,
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
                    onClick = onReport,
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
                    onClick = onProfile,
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
    }
}

