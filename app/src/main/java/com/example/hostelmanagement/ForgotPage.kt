package com.example.hostelmanagement

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ForgotPage(
    onResetNavigate: (docId: String) -> Unit,
    onBack: () -> Unit
) {
    var branchEmail by remember { mutableStateOf("") }
    var forgotSecret by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val masterSecret = "YYYYYYYY"
    val db = FirebaseFirestore.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDE2E4))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDE2E4))
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBack() }
                )
            }

            Spacer(modifier = Modifier.width(15.dp))

            Text(text = "Forgot Password",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF634035),
                fontSize = 45.sp)

            Spacer(Modifier.height(40.dp))

            OutlinedTextField(
                value = branchEmail,
                onValueChange = { branchEmail = it },
                label = {
                    Text(
                        text="Email",
                        color = Color(0xFF634035)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                ))

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = forgotSecret,
                onValueChange = { forgotSecret = it },
                label = {
                    Text(
                        text="Secret Word",
                        color = Color(0xFF634035)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                ))

            Spacer(Modifier.height(50.dp))

            Button(
                onClick = {
                    errorMessage = ""
                    val trimmedEmail = branchEmail.trim()
                    val trimmedSecret = forgotSecret.trim()

                    if (trimmedEmail.isEmpty() || trimmedSecret.isEmpty()) {
                        errorMessage = "Please fill in all fields."
                        return@Button
                    }

                    // Fetch document by email
                    db.collection("Branch")
                        .whereEqualTo("branchEmail", trimmedEmail)
                        .get()
                        .addOnSuccessListener { result ->
                            if (result.isEmpty) {
                                errorMessage = "This email does not exist."
                            } else {
                                if (trimmedSecret.equals(masterSecret, ignoreCase = true)) {
                                    val doc = result.documents[0]
                                    onResetNavigate(doc.id)
                                } else {
                                    errorMessage = "Secret word is incorrect."
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ForgotPage", "Firestore error", e)
                            errorMessage = "Error connecting to server."
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFCCF1F2),
                contentColor = Color(0xFF634035)
            ),
            shape = MaterialTheme.shapes.medium
            ){
                Text(
                    text = "Send Reset Request",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
