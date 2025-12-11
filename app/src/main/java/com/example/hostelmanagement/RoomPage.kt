package com.example.hostelmanagement

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.Manifest
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.forEach
import kotlin.text.all
import kotlin.text.isDigit
import kotlin.text.isEmpty
import kotlin.text.toDoubleOrNull
import kotlin.text.toIntOrNull
import kotlin.toString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomPage(
    hostelId:String,
    onDone:()->Unit,
    viewModel: RoomViewModel=viewModel()
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var lastError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
            Log.d("RoomPage", "Image selected: $uri")
        }
    }
    val permission =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pickImageLauncher.launch("image/*")
        } else {
            lastError = "Permission denied to access images."
            scope.launch { snackbarHostState.showSnackbar(lastError ?: "") }
        }
    }

    val types = listOf("Single", "Medium", "Master")
    val defaultCapacities = mapOf("Single" to 1, "Medium" to 2, "Master" to 3)

    var selectedType by remember { mutableStateOf(types[0]) }
    var expanded by remember { mutableStateOf(false) }
    var capacityText by remember { mutableStateOf(defaultCapacities[selectedType].toString()) }
    var priceText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }

    LaunchedEffect(selectedType) {
        capacityText = defaultCapacities[selectedType]?.toString() ?: capacityText
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier=Modifier.fillMaxWidth().background(Color(0xFFFDE2E4))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 8.dp, top = 25.dp, bottom = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    IconButton(onClick = { if (!isSaving) onDone() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                Text(
                    "Add Room",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize=22.sp,
                    color = Color(0xFF634035))
            } },
        containerColor = Color(0xFFFDE2E4)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.Top
        ) {
            if (imageUri != null) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Room Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            } else {
                Box(
                    modifier=Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { permissionLauncher.launch(permission) },
                        modifier = Modifier.width(180.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFCDE8FF),
                            contentColor = Color(0xFF4A3F35)
                        )
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "Select Image")
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Select Image", modifier = Modifier.padding(start = 8.dp))
                    }
                }

            }
            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Room Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = TextFieldDefaults.colors()
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.exposedDropdownSize()
                ) {
                    types.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t) },
                            onClick = {
                                selectedType = t
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = capacityText,
                onValueChange = { new ->
                    if (new.all { it.isDigit() } || new.isEmpty())
                        capacityText = new
                },
                label = { Text("Capacity(No. of student)") },
                modifier = Modifier.fillMaxWidth(),
                colors= TextFieldDefaults.colors(
                    focusedContainerColor=Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Price(RM)") },
                modifier = Modifier.fillMaxWidth(),
                colors= TextFieldDefaults.colors(
                    focusedContainerColor=Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = descriptionText,
                onValueChange = { descriptionText = it },
                label = { Text("Description") },
                colors= TextFieldDefaults.colors(
                    focusedContainerColor=Color(0xFFCCF1F2),
                    unfocusedContainerColor = Color(0xFFCCF1F2)
                ),
                modifier = Modifier.fillMaxWidth().height(100.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier=Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (isSaving) return@Button
                        val roomCapacity = capacityText.toIntOrNull()
                        if (roomCapacity == null || roomCapacity <= 0 || roomCapacity>=5) {
                            //lastError = "Invalid capacity"
                            scope.launch { snackbarHostState.showSnackbar("Please enter a valid capacity (min 1 , max 4)") }
                            return@Button
                        }
                        val roomPrice = priceText.toDoubleOrNull()
                        if (roomPrice == null || roomPrice <= 0.0) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter a valid price") }
                            return@Button
                        }
                        scope.launch {
                            isSaving = true
                            lastError = null
                            try {
                                val roomId = viewModel.addRoomWithDetails(
                                    hostelId = hostelId,
                                    selectedImageUri = imageUri,
                                    roomType = selectedType,
                                    roomCapacity = roomCapacity,
                                    roomPrice = roomPrice,
                                    roomDescription = descriptionText,
                                )
                                snackbarHostState.showSnackbar("Room Created: $roomId")
                                delay(1500)
                                onDone()
                            } catch (e: Exception) {
                                lastError = e.message
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    modifier = Modifier.width(180.dp).shadow(4.dp, RoundedCornerShape(16.dp)),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9DFCFF),
                        contentColor = Color(0xFF634035)
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                    } else {
                        Text("Save")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier=Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { if (!isSaving) onDone() },
                    modifier = Modifier.width(180.dp).shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB89278),
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancel")
                }
                lastError?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
