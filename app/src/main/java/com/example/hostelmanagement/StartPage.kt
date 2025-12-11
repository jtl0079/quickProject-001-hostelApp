package com.example.hostelmanagement

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StartPage(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
){
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
                .background(Color(0xFFFDE2E4)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(320.dp)
                    .height(375.dp)
        )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to Daily Hostel",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 60.sp,
                lineHeight = 60.sp,
                maxLines = 3,
                softWrap = true,
                textAlign = TextAlign.Center,
                color = Color(0xFF634035),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onNavigateToRegister,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9DFCFF)),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(60.dp)) {
                    Text(text = "Register",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF634035))
                }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = onNavigateToLogin,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9DFCFF)),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(60.dp)) {
                    Text(text = "Login",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF634035))
                }
            }
        }
    }