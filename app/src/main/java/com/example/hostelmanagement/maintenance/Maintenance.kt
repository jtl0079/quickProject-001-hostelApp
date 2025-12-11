package com.example.hostelmanagement.maintenance


data class MaintenanceItems(
    val category : String,
    val id : Int,
    val item : String,
    val issue : String //need to remove id
){
    constructor(): this("", 100, "", "")
}

data class Maintenance(
    val maintenanceId : String,
    val roomId : String,
    val item : List<MaintenanceItems>,
    val status : MaintenanceStatus?,
    val requestDate : String,
    val resolveDate : String,
    val remarks : String
){
    constructor(): this("", "", emptyList(), null, "", "", "")
}
