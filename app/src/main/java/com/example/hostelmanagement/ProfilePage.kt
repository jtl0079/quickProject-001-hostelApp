package com.example.hostelmanagement

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hostelmanagement.maintenance.MaintenanceViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfilePage(
    onLogout: () -> Unit,
    onHome: () -> Unit,
    onBooking: () -> Unit,
    onReport: () -> Unit,
    onProfile: () -> Unit,
    maintenanceViewModel: MaintenanceViewModel,

) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var branchName by remember { mutableStateOf("Loading...") }
    var branchEmail by remember { mutableStateOf("Loading...") }
    var branchPhoneNumber by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var documentId by remember { mutableStateOf("") }


    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val app = context.applicationContext as com.example.hostelmanagement.booking.App
    val repository = app.bookingRepository
    val database = app.database

    LaunchedEffect(Unit) {
        val userEmail = currentUser?.email
        val uid = currentUser?.uid

        if (uid.isNullOrEmpty()) {
            Log.e("ProfilePage", "No user logged in")
            message = "No user logged in"
            isLoading = false
            return@LaunchedEffect
        }

        // Fetch from Firebase using UID
        db.collection("Branch")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("ProfilePage", "Document found")
                    documentId = document.id
                    // Use the correct field names from registration
                    branchName = document.getString("branchName") ?: "N/A"
                    branchEmail = document.getString("branchEmail") ?: userEmail ?: "N/A"
                    branchPhoneNumber = document.getString("branchPhoneNumber") ?: "N/A"
                    isLoading = false
                    Log.d("ProfilePage", "Data loaded: username=$branchName, phone=$branchPhoneNumber, email=$branchEmail")
                } else {
                    // Fallback: search by email field
                    Log.d("ProfilePage", "Document not found with UID, searching by email field")
                    if (userEmail != null) {
                        db.collection("Branch")
                            .whereEqualTo("branchEmail", userEmail)  // Changed to branchEmail
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                if (!querySnapshot.isEmpty) {
                                    val doc = querySnapshot.documents[0]
                                    documentId = doc.id
                                    branchName = doc.getString("branchName") ?: "N/A"
                                    branchEmail = doc.getString("branchEmail") ?: userEmail
                                    branchPhoneNumber = doc.getString("branchPhoneNumber") ?: "N/A"
                                    isLoading = false
                                    Log.d("ProfilePage", "Data loaded from query: username=$branchName")
                                } else {
                                    Log.e("ProfilePage", "No user document found")
                                    message = "User profile not found in database"
                                    branchName = "Not Found"
                                    branchEmail = userEmail
                                    branchPhoneNumber = "Not Found"
                                    isLoading = false
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("ProfilePage", "Query failed", e)
                                message = "Error: ${e.message}"
                                isLoading = false
                            }
                    } else {
                        message = "Email not found"
                        isLoading = false
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfilePage", "Error fetching document", e)
                message = "Error loading profile: ${e.message}"
                isLoading = false
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
                .background(Color(0xFFFDE2E4))
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "User Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF634035),
                fontSize = 45.sp)

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading profile...",
                        modifier = Modifier.padding(top = 60.dp)
                    )
                }
            } else {
                // Username
                Text(
                    text = "Username",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF634035)
                )
                OutlinedTextField(
                    value = branchName,
                    onValueChange = { branchName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(
                        text = "Enter username",
                        color = Color(0xFF634035)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color(0xFF634035),
                        disabledBorderColor = Color(0xFF634035),
                        disabledContainerColor = Color(0xFFCCF1F2),
                        focusedContainerColor = Color(0xFFCCF1F2),
                        unfocusedContainerColor = Color(0xFFCCF1F2)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email (Read-only)
                Text(
                    text = "Email",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF634035)
                )
                OutlinedTextField(
                    value = branchEmail,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color(0xFF000000),
                        disabledBorderColor = Color(0xFF634035),
                        disabledContainerColor = Color(0xFFCCF1F2),
                        focusedContainerColor = Color(0xFFCCF1F2),
                        unfocusedContainerColor = Color(0xFFCCF1F2)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone Number
                Text(
                    text = "Phone No.",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF634035)
                )
                OutlinedTextField(
                    value = branchPhoneNumber,
                    onValueChange = { branchPhoneNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "Enter phone number",
                            color = Color(0xFF634035)
                        ) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color(0xFF634035),
                        disabledBorderColor = Color(0xFF634035),
                        disabledContainerColor = Color(0xFFCCF1F2),
                        focusedContainerColor = Color(0xFFCCF1F2),
                        unfocusedContainerColor = Color(0xFFCCF1F2)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Display message
                if (message.isNotEmpty()) {
                    Text(
                        text = message,
                        color = if (message.contains("success", ignoreCase = true))
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Update Profile Button
                Button(
                    onClick = {
                        if (branchName.isBlank() || branchName == "N/A" || branchName == "Loading..." || branchName == "Not Found") {
                            message = "Please enter a valid username"
                            return@Button
                        }

                        if (branchPhoneNumber.isBlank() || branchPhoneNumber == "N/A" || branchPhoneNumber == "Loading..." || branchPhoneNumber == "Not Found") {
                            message = "Please enter a valid phone number"
                            return@Button
                        }

                        if (documentId.isEmpty()) {
                            message = "Cannot update: User document not found"
                            return@Button
                        }

                        isSaving = true
                        message = "Updating..."

                        // Update using correct field names
                        val updates = hashMapOf(
                            "branchName" to branchName,
                            "branchPhoneNumber" to branchPhoneNumber
                        )

                        db.collection("Branch")
                            .document(documentId)
                            .update(updates as Map<String, Any>)
                            .addOnSuccessListener {
                                isSaving = false
                                message = "✓ Profile updated successfully!"
                                Log.d("ProfilePage", "Profile updated successfully")
                            }
                            .addOnFailureListener { e ->
                                isSaving = false
                                message = "Failed to update database: ${e.message}"
                                Log.e("ProfilePage", "Firestore update failed", e)
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9DFCFF)
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = if (isSaving) "Updating..." else "Update Profile",color = Color(0xFF634035))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Logout Button
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                repository.stopSync()
                                try {
                                    repository.clearLocalBookings()
                                } catch (e: Exception) {
                                    withContext(Dispatchers.IO) {
                                        database.clearAllTables()
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    auth.signOut()
                                    onLogout()
                                    maintenanceViewModel.clearUIdAndHostelId()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF8787)
                    )
                ) {
                    Text(text = "Logout",
                        color = Color(0xFF634035))
                }
            }
        }

        // Bottom Navigation
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