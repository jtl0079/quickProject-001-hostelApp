package com.example.hostelmanagement

import kotlin.collections.sortedWith
import kotlin.text.filter
import kotlin.text.isDigit
import kotlin.text.isLetter
import kotlin.text.isNullOrEmpty
import kotlin.text.take
import kotlin.text.toIntOrNull
import kotlin.text.uppercase

object RoomIdUtils {

    fun createPrefix(hostelName: String): String {
        val letters = hostelName.filter { it.isLetter() }.uppercase()
        return when(letters.length) {
            0->"HSTSR"
            1->letters+"XXXX"
            2->letters+"XXX"
            3->letters+"XX"
            4->letters+"X"
            else->letters.take(5)
        }
    }

    fun extractNumber(id: String?): Int {
       if(id.isNullOrEmpty()) return 0
        val match = Regex("\\d+$").find(id)?:return 0
        return match.value.toIntOrNull()?:0
    }

    fun sortRooms(list:List<RoomItem>):List<RoomItem>{
        return list.sortedWith(compareBy(
            {item -> item.roomId.takeWhile { ch -> !ch.isDigit()}},
            {item -> extractNumber(item.roomId)}
        ))
    }
}
