package com.example.hostelmanagement

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value) }

    fun togglePasswordVisibility() =
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun login(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        when {
            email.isEmpty() || password.isEmpty() ->
                onError("Please fill in all fields")

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                onError("Invalid email address")

            else -> {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSuccess()
                        } else {
                            onError(task.exception?.message ?: "Login failed")
                        }
                    }
            }
        }
    }
}
