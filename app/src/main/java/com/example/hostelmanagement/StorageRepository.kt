package com.example.hostelmanagement

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class StorageRepository(
    private val storage : FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun uploadRoomImage(branchId:String,hostelId:String,imageUri: Uri):String=
        suspendCancellableCoroutine { cont ->
            val path = "Hostel/$branchId/$hostelId/${System.currentTimeMillis()}.jpg"
            val ref = storage.reference.child(path)
            val uploadTask = ref.putFile(imageUri)

            uploadTask.addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    cont.resume(uri.toString())
                }.addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
            }.addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
            cont.invokeOnCancellation {
                uploadTask.cancel()
            }
        }

}