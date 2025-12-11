/*package com.example.hostelmanagement

data class RegistrationUiState (
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword : String ="",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible : Boolean =false,
    val isLoading: Boolean = false,

)*/

package com.example.hostelmanagement

data class RegistrationUiState(
    val branchName: String = "",
    val branchEmail: String = "",
    val branchPassword: String = "",
    val branchConfirmPassword: String = "",
    val branchPhoneNumber: String = "",
    val branchSecretWord: String = "XXXXXXXX",
    val branchPasswordVisible: Boolean = false,
    val branchConfirmPasswordVisible: Boolean = false,
)

