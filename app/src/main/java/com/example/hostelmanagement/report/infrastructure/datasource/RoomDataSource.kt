package com.example.hostelmanagement.report.infrastructure.datasource




import com.example.hostelmanagement.RoomItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object RoomDataSource {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * 读取【单个 hostel】下的所有 rooms
     */
    suspend fun loadRoomsByHostel(hostelId: String): List<RoomItem> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        val safeHostelId = hostelId
            .trim()
            .trim('/')
            .split('/')
            .last()

        val snapshot = firestore.collection("Branch")
            .document(uid)
            .collection("Hostel")
            .document(safeHostelId)
            .collection("rooms")
            .get()
            .await()

        return snapshot.documents.map { d ->
            RoomItem(
                roomId = d.getString("roomId") ?: d.id,
                photoUrl = d.getString("photoUrl"),
                roomType = d.getString("roomType"),
                roomCapacity = d.getLong("roomCapacity")?.toInt(),
                roomPrice = d.getDouble("roomPrice"),
                roomDescription = d.getString("roomDescription"),
                createdAt = d.getTimestamp("createdAt"),
                roomStatus = d.getString("roomStatus") ?: "Available",
                hostelId = safeHostelId
            )
        }
    }

    /**
     * 读取【整个 branch】下的所有 rooms（跨 hostel）
     */
    suspend fun loadAllRoomsInBranch(): List<RoomItem> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        val hostelsSnapshot = firestore.collection("Branch")
            .document(uid)
            .collection("Hostel")
            .get()
            .await()

        if (hostelsSnapshot.isEmpty) return emptyList()

        val allRooms = mutableListOf<RoomItem>()

        for (hostelDoc in hostelsSnapshot.documents) {
            val hostelId = hostelDoc.id
            val roomsSnap = hostelDoc.reference
                .collection("rooms")
                .get()
                .await()

            for (d in roomsSnap.documents) {
                allRooms.add(
                    RoomItem(
                        roomId = d.getString("roomId") ?: d.id,
                        photoUrl = d.getString("photoUrl"),
                        roomType = d.getString("roomType"),
                        roomCapacity = d.getLong("roomCapacity")?.toInt(),
                        roomPrice = d.getDouble("roomPrice"),
                        roomDescription = d.getString("roomDescription"),
                        createdAt = d.getTimestamp("createdAt"),
                        roomStatus = d.getString("roomStatus") ?: "Available",
                        hostelId = hostelId
                    )
                )
            }
        }
        return allRooms
    }

    /**
     * 通过 roomId 查找 Room + 它所属的 hostelId
     */
    suspend fun findRoomByRoomId(roomId: String): RoomItem? {
        val uid = auth.currentUser?.uid ?: return null

        val hostelsSnapshot = firestore.collection("Branch")
            .document(uid)
            .collection("Hostel")
            .get()
            .await()

        if (hostelsSnapshot.isEmpty) return null

        for (hostelDoc in hostelsSnapshot.documents) {
            val roomsSnapshot = hostelDoc.reference
                .collection("rooms")
                .whereEqualTo("roomId", roomId)
                .get()
                .await()

            if (!roomsSnapshot.isEmpty) {
                val d = roomsSnapshot.documents.first()
                return RoomItem(
                    roomId = d.getString("roomId") ?: d.id,
                    photoUrl = d.getString("photoUrl"),
                    roomType = d.getString("roomType"),
                    roomCapacity = d.getLong("roomCapacity")?.toInt(),
                    roomPrice = d.getDouble("roomPrice"),
                    roomDescription = d.getString("roomDescription"),
                    createdAt = d.getTimestamp("createdAt"),
                    roomStatus = d.getString("roomStatus") ?: "Available",
                    hostelId = hostelDoc.id
                )
            }
        }
        return null
    }
}
