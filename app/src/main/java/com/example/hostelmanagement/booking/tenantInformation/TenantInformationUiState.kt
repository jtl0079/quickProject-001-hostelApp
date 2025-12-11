package com.example.hostelmanagement.booking.tenantInformation

data class TenantInformationUiState (
    val tenantId :String = "",
    val tenantName:String = "",
    val phoneNumber:String = "",
    val email:String = "",
    val duration: List<String> = emptyList(),
    val error:String = "",
    val errorMessage:String = "",
    val tenant:List<TenantEntity> = emptyList()
    )