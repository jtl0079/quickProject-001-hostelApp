package com.example.hostelmanagement.maintenance

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.filter

class MaintenanceViewModel : ViewModel(){

    private val repository : MaintenanceRepository = MaintenanceRepository()

    private val _maintenanceList = MutableStateFlow<List<Maintenance>>(emptyList())
    val maintenanceList: StateFlow<List<Maintenance>> = _maintenanceList.asStateFlow()

    private val _maintenanceDetails = MutableStateFlow<Maintenance?>(null)
    val maintenanceDetails: StateFlow<Maintenance?> = _maintenanceDetails.asStateFlow()

    private val _showEmptyMessage = MutableStateFlow(false)
    val showEmptyMessage : StateFlow<Boolean> = _showEmptyMessage.asStateFlow()

    private val _failedDelete = MutableStateFlow("")
    val failedDelete : StateFlow<String> = _failedDelete.asStateFlow()

    private val _failedRead = MutableStateFlow("")
    val failedRead : StateFlow<String> = _failedRead.asStateFlow()

    private val _failedReadAll = MutableStateFlow("")
    val failedReadAll : StateFlow<String> = _failedReadAll.asStateFlow()

    private val _isSubmitted = MutableStateFlow(false)
    val isSubmitted : StateFlow<Boolean> = _isSubmitted.asStateFlow()

    val remarks = MutableStateFlow("")

    private val _show =  MutableSharedFlow<String>()
    val show = _show

    val selectedUId = MutableStateFlow("")
    val selectedHostelId = MutableStateFlow("")
    val selectedRoomId = MutableStateFlow("")

    private val _editMaintenance = MutableStateFlow<Maintenance?>(null)
    val editMaintenance = _editMaintenance.asStateFlow()

    fun clearUIdAndHostelId(){
        selectedHostelId.value = ""
        selectedUId.value = ""
    }

    fun clearRoomId(){
        selectedRoomId.value = ""
    }
    fun clearDelete(){
        _failedDelete.value = ""
    }

    fun clearRead(){
        _failedRead.value = ""
    }

    fun clearReadAll(){
        _failedReadAll.value = ""
    }
    fun submitSuccess(){
        _isSubmitted.value = true
    }

    fun resetSubmitValue(){
        _isSubmitted.value = false
    }
    fun requestEmptyMessage(){
        _showEmptyMessage.value = true
    }

    fun closeEmptyMessage(){
        _showEmptyMessage.value = false
    }

    fun edit(maintenance: Maintenance){
        _editMaintenance.value = maintenance
    }

    fun generateMaintenanceId(): String{
        val timestamp = System.currentTimeMillis()
        return "M${timestamp}"
    }

    fun generateDateTime(): String{
        val time = System.currentTimeMillis()
        val s = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return s.format(Date(time))
    }

    fun clearAll(){
        remarks.value = ""
        _maintenanceList.value = emptyList()
        _maintenanceDetails.value = null
        _showEmptyMessage.value = false
        _editMaintenance.value = null
    }

    fun createMaintenance(
        roomId: String,
        maintenanceList: List<MaintenanceItems>
    ) {

        if(maintenanceList.isEmpty() &&remarks.value.isEmpty()){
            requestEmptyMessage()

        }else{

            val maintenanceId = generateMaintenanceId()
            val dateTime = generateDateTime()
            val maintenanceStatus = MaintenanceStatus.IN_PROGRESS

            val newMaintenance = Maintenance(
                maintenanceId = maintenanceId,
                roomId = roomId,
                item = maintenanceList,
                status = maintenanceStatus,
                requestDate = dateTime,
                resolveDate = "",
                remarks = remarks.value

            )

            _maintenanceDetails.value = newMaintenance

            repository.saveMaintenanceToFirebase(
                selectedUId.value,
                selectedHostelId.value,
                selectedRoomId.value,
                newMaintenance,
                onSuccess = {
                    Log.d("Firebase", "Maintenance request created successfully")
                    submitSuccess()
                    repository.updateRoomStatus(
                        selectedUId.value,
                        selectedHostelId.value,
                        selectedRoomId.value

                    )
                },
                onError = { e ->
                    Log.e("Firebase", "Failed to create maintenance request", e)

                })

        }

    }

    fun readMaintenance(
        maintenanceId: String
    ){
        repository.readMaintenanceFromFirebase(
            selectedUId.value,
            selectedHostelId.value,
            selectedRoomId.value,
            maintenanceId = maintenanceId,
            onResult = { maintenanceData ->
                _maintenanceDetails.value = maintenanceData
            },
            onError = { e->
                _failedRead.value = "Read failed: ${e.message}"
            }
        )

    }

    fun readMaintenanceHistory(
        roomId: String
    ){
        repository.readAllMaintenancesFromFirebase(
            selectedUId.value,
            selectedHostelId.value,
            roomId,
            onSuccess = { maintenanceList ->
                _maintenanceList.value = maintenanceList
            },
            onError = { e->
                _failedReadAll.value = "Read failed: ${e.message}"
            }
        )
    }

    fun updateMaintenanceStatusResolved(
        updateMaintenance: Maintenance,

    ){
        val newStatus = MaintenanceStatus.COMPLETED
        val resolveDate = generateDateTime()
        val updatedMaintenance = updateMaintenance.copy(status = newStatus, resolveDate = resolveDate)

        _maintenanceDetails.value = updatedMaintenance
        repository.updateMaintenanceInFirebase(
            selectedUId.value,
            selectedHostelId.value,
            selectedRoomId.value,
            maintenanceId = updateMaintenance.maintenanceId,
            updates = mapOf(
                "status" to newStatus.name,
                "resolveDate" to resolveDate
            ),
            onSuccess = {
                Log.d("Firebase", "Maintenance resolved successfully")
                repository.updateRoomStatus(
                    selectedUId.value,
                    selectedHostelId.value,
                    selectedRoomId.value

                )

            },
            onError = { e ->
                Log.e("Firebase", "Failed to resolve maintenance", e)

            }
        )

        readMaintenanceHistory(selectedRoomId.value)
        //when update then read again, the state flow know the history got change so ui will recompose

        viewModelScope.launch{
            _show.emit("Maintenance request marked as completed")
        }
    }

    fun updateMaintenanceItem(
        updateMaintenance: Maintenance,
        newItemList : List<MaintenanceItems>,
        newRemarks : String
    ){
        val updatedMaintenance = updateMaintenance.copy(item = newItemList, remarks = newRemarks)

        _maintenanceDetails.value = updatedMaintenance
        repository.updateMaintenanceInFirebase(
            selectedUId.value,
            selectedHostelId.value,
            selectedRoomId.value,
            maintenanceId = updateMaintenance.maintenanceId,
            updates = mapOf(
                "item" to newItemList,
                "remarks" to newRemarks
            ),
            onSuccess = {
                Log.d("Firebase", "Maintenance request updated successfully")
            },
            onError = { e ->
                Log.e("Firebase", "Failed to update maintenance item", e)
            }
        )

        viewModelScope.launch{
            _show.emit("Maintenance request is update successfully.")
        }
        submitSuccess()

    }



    fun deleteMaintenance(
        deleteMaintenance: Maintenance,
    ){

        repository.deleteMaintenanceFromFirebase(
            selectedUId.value,
            selectedHostelId.value,
            selectedRoomId.value,
            maintenanceId = deleteMaintenance.maintenanceId,
            onSuccess = {
                if(_maintenanceDetails.value?.maintenanceId == deleteMaintenance.maintenanceId){
                    _maintenanceDetails.value = null
                }
                val updatedMaintenanceList = _maintenanceList.value.filter{
                    it.maintenanceId != deleteMaintenance.maintenanceId
                }

                _maintenanceList.value = updatedMaintenanceList
                repository.updateRoomStatus(
                    selectedUId.value,
                    selectedHostelId.value,
                    selectedRoomId.value

                )
            },
            onError = { e ->
                _failedDelete.value = "Delete failed: ${e.message}"

            }
        )

        viewModelScope.launch{
            _show.emit("Maintenance request are deleted successfully.")
        }

    }


}