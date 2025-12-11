package com.example.hostelmanagement

import com.google.firebase.Timestamp

data class RoomItem(
    val roomId:String,
    val photoUrl:String?,
    val roomType:String?=null,
    val roomCapacity:Int?=null,
    val roomPrice:Double?=null,
    val roomDescription:String?=null,
    val createdAt: Timestamp?=null,
    val roomStatus: String = "Available",//
    val hostelId:String?=null//
)
