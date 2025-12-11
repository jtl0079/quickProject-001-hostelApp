package com.example.hostelmanagement

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun ResetPage(
    docId: String,
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit
) {
    var pass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    var confirmPassVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

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
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF634035),
                fontSize = 45.sp
            )

            Spacer(Modifier.height(40.dp))

            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = {
                    Text(
                        text = "New Password",
                        color = Color(0xFF634035)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                ),
                visualTransformation =
                    if (passVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passVisible = !passVisible }) {
                        Image(
                            painter = painterResource(
                                id = if (passVisible)
                                    R.drawable.eye
                                else
                                    R.drawable.eye_close
                            ),
                            contentDescription = "Toggle Password Visibility"
                        )
                    }
                }
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = confirmPass,
                onValueChange = { confirmPass = it },
                label = {
                    Text(
                        text = "Confirm New Password",
                        color = Color(0xFF634035)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                ),
                visualTransformation =
                    if (confirmPassVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPassVisible = !confirmPassVisible }) {
                        Image(
                            painter = painterResource(
                                id = if (confirmPassVisible)
                                    R.drawable.eye
                                else
                                    R.drawable.eye_close
                            ),
                            contentDescription = "Toggle Password Visibility"
                        )
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            Row {
                Button(
                    onClick = {
                        message = ""
                        if (pass.isEmpty() || confirmPass.isEmpty()) {
                            message = "Please fill in both fields."
                            return@Button
                        }
                        if (pass != confirmPass) {
                            message = "Passwords do not match."
                            return@Button
                        }
                        if (pass.length < 6) {
                            message = "Password must be at least 6 characters."
                            return@Button
                        }

                        isLoading = true

                        db.collection("Branch").document(docId)
                            .get()
                            .addOnSuccessListener { document ->
                                val branchEmail = document.getString("branchEmail")
                                val oldPassword = document.getString("branchPassword")

                                if (branchEmail != null && oldPassword != null) {
                                    auth.signInWithEmailAndPassword(branchEmail, oldPassword)
                                        .addOnSuccessListener {
                                            val branch = auth.currentUser
                                            branch?.updatePassword(pass)
                                                ?.addOnSuccessListener {
                                                    db.collection("Branch").document(docId)
                                                        .update("branchPassword", pass)
                                                        .addOnSuccessListener {
                                                            isLoading = false
                                                            message = "Password updated successfully!"
                                                            auth.signOut()
                                                            kotlinx.coroutines.CoroutineScope(
                                                                kotlinx.coroutines.Dispatchers.Main
                                                            ).launch {
                                                                kotlinx.coroutines.delay(1500)
                                                                onNavigateToLogin()
                                                            }
                                                        }
                                                }
                                        }
                                }
                            }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF634035),
                        contentColor = Color(0xFFFFFFFF)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(55.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF634035)
                        )
                    } else {
                        Text(
                            "Confirm",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.width(20.dp))

                Button(
                    onClick = onBack,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFDE2E4),
                        contentColor = Color(0xFF634035)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(55.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        "Cancel",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(15.dp))

            if (message.isNotEmpty()) {
                Text(
                    message,
                    color = if (message.contains("successfully"))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}