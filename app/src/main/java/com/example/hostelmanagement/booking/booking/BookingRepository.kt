package com.example.hostelmanagement.booking.booking

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java

class BookingRepository(private val dao: BookingDao) {

    val bookingsFlow: Flow<List<BookingEntity>> = dao.getAllBooking()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private var bookingListener: ListenerRegistration? = null

    fun startSyncFromFirebase() {
        bookingListener?.remove()

        val uid = auth.currentUser?.uid ?: return

        val collRef = firestore.collection("Branch")
            .document(uid)
            .collection("Booking")

        bookingListener = collRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            CoroutineScope(Dispatchers.IO).launch {
                for (change in snapshot.documentChanges) {
                    val doc = change.document
                    when (change.type) {
                        DocumentChange.Type.ADDED,
                        DocumentChange.Type.MODIFIED -> {
                            val fb = doc.toObject(FirebaseBooking::class.java)
                            val entity = fb.toEntity(doc.id)
                            dao.insertBooking(entity)
                        }
                        DocumentChange.Type.REMOVED -> {
                            dao.deleteById(doc.id)
                        }
                    }
                }
            }
        }
    }

    fun stopSync() {
        bookingListener?.remove()
        bookingListener = null
    }


    suspend fun insertBooking(booking: BookingEntity) {

        dao.insertBooking(booking)

        val uid = auth.currentUser?.uid ?: return

        firestore.collection("Branch")
            .document(uid)
            .collection("booking")
            .document(booking.bookingId)
            .set(booking.toFirebase())
            .await()
    }

    suspend fun insertAllBookings(bookings: List<BookingEntity>) {
        dao.insertAll(bookings)
    }

    fun getBookingUsingState(status: BookingStatus): Flow<List<BookingEntity>> {
        return dao.getBookingUsingStatus(status)
    }

    fun getAllBooking(): Flow<List<BookingEntity>> {
        return dao.getAllBooking()
    }

    suspend fun clearLocalBookings() {
        dao.deleteAllBookings()
    }

}
