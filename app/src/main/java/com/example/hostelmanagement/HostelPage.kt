package com.example.hostelmanagement

import android.R.attr.enabled
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.hostelmanagement.maintenance.MaintenanceViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@Composable
fun HostelPage(
    onCancel: () -> Unit,
    maintenanceViewModel: MaintenanceViewModel
) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    val hostelPicture = remember { mutableStateOf<String?>(null) }
    var hostelName by remember { mutableStateOf("") }
    var hostelAddress by remember { mutableStateOf("") }
    var hostelCity by remember { mutableStateOf("") }
    var hostelState by remember { mutableStateOf("") }
    var hostelPostalCode by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false)}

    val context = LocalContext.current
    val uid: String? = FirebaseAuth.getInstance().currentUser?.uid

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> hostelPicture.value = uri?.toString() }

    fun generateHostelId(index: Int): String {
        return "H" + index.toString().padStart(3, '0')
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDE2E4))
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDE2E4))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "New Hostel",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF634035),
            fontSize = 45.sp)

        if (hostelPicture.value != null) {
            Image(
                painter = rememberAsyncImagePainter(hostelPicture.value),
                contentDescription = "Selected Hostel Picture",
                modifier = Modifier
                    .size(150.dp)
                    .padding(16.dp)
                    .clickable { launcher.launch("image/*") } // <-- make image clickable
            )
        } else {
            // Show a placeholder image when nothing is selected
            Image(
                painter = painterResource(id = R.drawable.picture),
                contentDescription = "Select Hostel Picture",
                modifier = Modifier
                    .size(150.dp)
                    .padding(16.dp)
                    .clickable { launcher.launch("image/*") } // <-- also clickable
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = hostelName,
            onValueChange = { hostelName = it },
            label = { Text(
                text = "Hostel Name",
                color = Color(0xFF634035)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFCCF1F2),
                unfocusedContainerColor = Color(0xFFCCF1F2)
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = hostelAddress,
            onValueChange = { hostelAddress = it },
            label = { Text(
                text = "Hostel Address",
                color = Color(0xFF634035)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFCCF1F2),
                unfocusedContainerColor = Color(0xFFCCF1F2)
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = hostelCity,
            onValueChange = { hostelCity = it },
            label = { Text(
                text = "Hostel City",
                color = Color(0xFF634035)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFCCF1F2),
                unfocusedContainerColor = Color(0xFFCCF1F2)
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = hostelState,
            onValueChange = { hostelState = it },
            label = { Text(
                text = "Hostel State",
                color = Color(0xFF634035)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFCCF1F2),
                unfocusedContainerColor = Color(0xFFCCF1F2)
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = hostelPostalCode,
            onValueChange = { hostelPostalCode = it },
            label = { Text(
                text = "Hostel Postal Code",
                color = Color(0xFF634035)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFCCF1F2),
                unfocusedContainerColor = Color(0xFFCCF1F2)
            ),
        )

        Spacer(modifier = Modifier.height(50.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Button(
                onClick = {
                    if (isSaving) return@Button
                    isSaving = true

                    if (hostelName.isNotBlank() && uid != null) {
                        val userRef = db.collection("Branch").document(uid)
                        userRef.get().addOnSuccessListener { userSnapshot ->
                            val branchId = userSnapshot.getString("branchId") ?: "B000"
                            val hostelCollectionRef = userRef.collection("Hostel")
                            val inputPrefix = hostelName.trim().lowercase().take(5)

                            hostelCollectionRef.get().addOnSuccessListener { allHostels ->

                                var clash = false
                                for (doc in allHostels.documents) {
                                    val existingRaw = doc.getString("hostelName") ?: ""
                                    val existingPrefix = existingRaw.trim().lowercase().take(5)
                                    if (existingPrefix == inputPrefix) {
                                        clash = true
                                        break
                                    }
                                }
                                if (clash) {
                                    isSaving = false
                                    Toast.makeText(
                                        context,
                                        "Hostel Name first 5 word cannot be same",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@addOnSuccessListener
                                }
                                hostelCollectionRef
                                    .whereEqualTo("hostelName", hostelName)
                                    .get()
                                    .addOnSuccessListener { nameCheckSnapshot ->
                                        if (!nameCheckSnapshot.isEmpty) {
                                            isSaving = false
                                            Toast.makeText(
                                                context,
                                                " Hostel with the same name already exists!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@addOnSuccessListener
                                        }

                                        db.collection("Branch")
                                            .document(uid)
                                            .collection("Hostel")
                                            .get()
                                            .addOnSuccessListener { snapshot ->
                                                val newIndex = snapshot.size() + 1
                                                val hostelId = generateHostelId(newIndex)

                                                val uri = hostelPicture.value?.let { Uri.parse(it) }
                                                if (uri != null) {
                                                    val imageRef =
                                                        storage.reference.child("Hostel/$uid/$hostelId.jpg")
                                                    imageRef.putFile(uri)
                                                        .addOnSuccessListener {
                                                            imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                                                val newItem = HostelUiState(
                                                                    hostelId = hostelId,
                                                                    hostelPicture = downloadUrl.toString(),
                                                                    hostelName = hostelName,
                                                                    hostelAddress = hostelAddress,
                                                                    hostelCity = hostelCity,
                                                                    hostelState = hostelState,
                                                                    hostelPostalCode = hostelPostalCode,
                                                                    branchId = branchId
                                                                )
                                                                db.collection("Branch")
                                                                    .document(uid)
                                                                    .collection("Hostel")
                                                                    .document(hostelId)
                                                                    .set(newItem)
                                                                    .addOnSuccessListener { onCancel() }
                                                                    .addOnFailureListener { it.printStackTrace() }
                                                            }
                                                        }
                                                        .addOnFailureListener {
                                                            it.printStackTrace()
                                                            isSaving = false
                                                        }
                                                } else {
                                                    val newItem = HostelUiState(
                                                        hostelId = hostelId,
                                                        hostelPicture = "",
                                                        hostelName = hostelName,
                                                        hostelAddress = hostelAddress,
                                                        hostelCity = hostelCity,
                                                        hostelState = hostelState,
                                                        hostelPostalCode = hostelPostalCode,
                                                        branchId = branchId
                                                    )
                                                    db.collection("Branch")
                                                        .document(uid)
                                                        .collection("Hostel")
                                                        .document(hostelId)
                                                        .set(newItem)
                                                        .addOnSuccessListener { onCancel() }
                                                        .addOnFailureListener { it.printStackTrace() }
                                                }

                                                maintenanceViewModel.selectedUId.value = uid
                                                maintenanceViewModel.selectedHostelId.value =
                                                    hostelId

                                            }
                                    }
                            }
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9DFCFF)),
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(60.dp)
            ) {
                Text(
                    text = if (isSaving) "Loading..." else "Add",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF634035))
            }
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9DFCFF)),
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .height(60.dp)) {
                Text(
                    text = "Cancel",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF634035))
            }
        }
    }
}

