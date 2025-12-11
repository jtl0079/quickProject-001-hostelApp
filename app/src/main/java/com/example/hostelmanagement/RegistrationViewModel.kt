package com.example.hostelmanagement

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class RegistrationViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(RegistrationUiState(branchSecretWord = ""))

    val uiState: StateFlow<RegistrationUiState> = _uiState

    fun onUsernameChange(value: String) = _uiState.update { it.copy(branchName = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(branchEmail = value) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(branchPassword = value) }
    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(branchConfirmPassword = value) }

    fun onPhoneNumberChange(value: String) = _uiState.update { it.copy(branchPhoneNumber = value) }
    fun onSecretWordChange(value: String) = _uiState.update { it.copy(branchSecretWord = value) }

    fun togglePasswordVisibility() =
        _uiState.update { it.copy(branchPasswordVisible = !it.branchPasswordVisible) }

    fun toggleConfirmPasswordVisibility() =
        _uiState.update { it.copy(branchConfirmPasswordVisible = !it.branchConfirmPasswordVisible) }

    private fun generateUserId(onResult: (String) -> Unit) {
        val counterRef = db.collection("counters").document("Branch")

        db.runTransaction { transaction ->
            val snapshot = transaction.get(counterRef)
            val lastId = snapshot.getLong("lastUserId") ?: 0
            val newId = lastId + 1

            val formattedId = "B" + newId.toString().padStart(3, '0')

            transaction.set(counterRef, mapOf("lastUserId" to newId))

            formattedId
        }.addOnSuccessListener { newId ->
            onResult(newId)
        }.addOnFailureListener {
            onResult("U000") // fallback
        }
    }

    fun registerUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val branchName = _uiState.value.branchName.trim()
        val branchEmail = _uiState.value.branchEmail.trim()
        val branchPassword = _uiState.value.branchPassword
        val branchConfirmPassword = _uiState.value.branchConfirmPassword
        val branchPhoneNumber = _uiState.value.branchPhoneNumber
        val branchSecretWord = _uiState.value.branchSecretWord
        val branchPresetSecret = "XXXXXXXX"

        when {
            branchName.isEmpty() || branchEmail.isEmpty() || branchPassword.isEmpty() || branchConfirmPassword.isEmpty() ||
                    branchPhoneNumber.isEmpty() || branchSecretWord.isEmpty() ->
                onError("All fields are required")

            !android.util.Patterns.EMAIL_ADDRESS.matcher(branchEmail).matches() ->
                onError("Invalid email address")

            branchPassword.length < 6 ->
                onError("Password must be at least 6 characters")

            branchPassword != branchConfirmPassword ->
                onError("Passwords do not match")

            branchSecretWord != branchPresetSecret ->
                onError("Secret Word is incorrect")

            else -> {
                auth.createUserWithEmailAndPassword(branchEmail, branchPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUid = auth.currentUser?.uid ?: ""

                            generateUserId { newUserId ->
                                val userMap = hashMapOf(
                                    "branchId" to newUserId,
                                    "branchName" to branchName,
                                    "branchEmail" to branchEmail,
                                    "branchPhoneNumber" to branchPhoneNumber,
                                    "branchPassword" to branchPassword,
                                    "branchSecretWord" to branchSecretWord,

                                )

                                db.collection("Branch").document(firebaseUid)
                                    .set(userMap)
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener { e ->
                                        onSuccess()
                                    }
                            }
                        } else {
                            val message = task.exception?.message ?: "Registration failed"
                            onError(message)
                        }
                    }
            }
        }
    }
}