package com.example.hostelmanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable

fun RoomCard(
    room: RoomItem,
    onClick:(RoomItem)->Unit,
    onDelete:(RoomItem)->Unit
    ){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick(room) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
    ) {
        Row(
            modifier=Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier= Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp)),
            ) {
                if (room.photoUrl.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Photo",
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    AsyncImage(
                        model = room.photoUrl,
                        contentDescription = "Room Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier=Modifier.weight(1f)
            ){
                Text(text = "Room ID: ${room.roomId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF634035)
                )
                Spacer(modifier=Modifier.height(4.dp))
                Text(text = room.roomType?:"",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF634035))
            }

            IconButton(onClick={onDelete(room)},modifier=Modifier.size(40.dp)){
                Icon(
                    imageVector= Icons.Default.Delete,
                    contentDescription = "Delete Room",
                    tint=Color(0xFFD32F2F)
                )
            }
        }
    }
}
