package com.example.hostelmanagement

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun Registration(
    viewModel: RegistrationViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDE2E4))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .background(Color(0xFFFDE2E4)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Hello! Register to get Started",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF634035),
                fontSize = 45.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.branchName,
                onValueChange = { viewModel.onUsernameChange(it) },
                label = {
                    Text(
                        text = "Username",
                        color = Color(0xFF634035)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.branchEmail,
                onValueChange = { viewModel.onEmailChange(it) },
                label = {
                    Text(
                        text="Email",
                        color = Color(0xFF634035)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.branchPassword,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = {
                    Text(
                        text="Password",
                        color = Color(0xFF634035)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                ),
                visualTransformation =
                    if (uiState.branchPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                        Image(
                            painter = painterResource(
                                id = if (uiState.branchPasswordVisible)
                                    R.drawable.eye
                                else
                                    R.drawable.eye_close
                            ),
                            contentDescription = "Toggle Password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = uiState.branchConfirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                label = {
                    Text(
                        text="Confirm Password",
                        color = Color(0xFF634035)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                ),
                visualTransformation =
                    if (uiState.branchConfirmPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                        Image(
                            painter = painterResource(
                                id = if (uiState.branchConfirmPasswordVisible)
                                    R.drawable.eye
                                else
                                    R.drawable.eye_close
                            ),
                            contentDescription = "Toggle Password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = uiState.branchPhoneNumber,
                onValueChange = { viewModel.onPhoneNumberChange(it) },
                label = {
                    Text(
                        text="Phone No.",
                        color = Color(0xFF634035)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = uiState.branchSecretWord,
                onValueChange = { viewModel.onSecretWordChange(it) },  // user types it
                label = {
                    Text(
                        text="Secret Word",
                        color = Color(0xFF634035)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                ),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    viewModel.registerUser(
                        onSuccess = { onNavigateToLogin() },
                        onError = { errorMessage = it }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFCCF1F2),
                    contentColor = Color(0xFF634035)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Register",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 16.sp
                )

                TextButton(onClick = { onNavigateToLogin() }) {
                    Text(
                        text = "Login Now",
                        color = Color(0xFFDA0303),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}