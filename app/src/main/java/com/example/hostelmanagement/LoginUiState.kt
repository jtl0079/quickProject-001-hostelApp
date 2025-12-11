package com.example.hostelmanagement

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false
)
