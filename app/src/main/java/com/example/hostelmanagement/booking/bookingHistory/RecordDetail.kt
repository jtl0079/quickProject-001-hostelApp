/*package com.example.hostelmanagement.YeoWei.bookingHistory

import android.graphics.Color.blue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hostelmanagement.YeoWei.bookingHistory.booking.BookingEntity
import kotlin.collections.find

@Composable
fun BookingDetails(
    bookingId: String,
    onDismiss: () -> Unit,
    bookingList: List<BookingEntity>
){
    val bookingList = bookingList


    val bookingDetails = bookingList.find({it.bookingId == bookingId})
    var show by remember{ mutableStateOf(false)}

    LaunchedEffect(Unit){
        show = true
    }

    val alpha by animateFloatAsState(//control the transparency of the pop up
        targetValue = if(bookingDetails != null && show) 1f else 0f,//if maintenance details are not null will be show
        animationSpec = tween(500)//in the 200ms will show the pop up
    )
    //animateFloatAsState = calculates animation values per frame
    //animateFloatAsState calculates the current animation value, and compose uses that value to redraw the Ui for each frame

    val scale by animateFloatAsState(//control the size of the pop up
        targetValue = if(bookingDetails != null && show) 1f else 0.5f,//if the maintenance details is not null will be show the target size
        animationSpec = tween(500)//in the 200ms will from small to big
    )
    Box(//to cover the Big Box and show the pop up so the color is black transparent (dim background)
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f* alpha))//follow the animation become dim background
            .clickable(
                indication = null,
                interactionSource = remember {MutableInteractionSource()}
            ){
                onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(//this is the pop up that show the maintenance details
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
                .size(360.dp, 500.dp)
                .background(Color.White)
                .border(2.dp, Color.Black)
                .clickable(enabled = false){},
            contentAlignment = Alignment.TopCenter
        ){
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "Booking ID : ${bookingDetails?.bookingId}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)

                )

                //WordManage("Room ID", maintenanceDetails?.roomId?: "")

                //WordManage("Request Date ", maintenanceDetails?.requestDate?: "")

                //WordManage("Status", maintenanceDetails?.status.toString())


                Card(
                    modifier = Modifier
                        .width(350.dp)
                        .padding(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = blue
                    ),
                    shape = RoundedCornerShape(16.dp)
                ){
                    bookingDetails?.item?.forEach{ item ->

                        //WordManage("Category", item.category)

                        //WordManage("Item Name", item.item)

                        //WordManage("Issue", item.issue)


                        Divider(Modifier.padding(vertical = 2.dp))

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Card(
                    modifier = Modifier
                        .width(350.dp)
                        .padding(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(16.dp)
                ){
                    WordManage("Remarks", maintenanceDetails?.remarks?: "")
                }


            }
        }
    }

}*/