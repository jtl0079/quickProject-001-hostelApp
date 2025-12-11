package com.example.hostelmanagement.maintenance

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.collections.filter
import kotlin.collections.find
import kotlin.collections.map
import kotlin.collections.toMutableList
import kotlin.collections.toMutableSet
import kotlin.collections.union

class ItemViewModel : ViewModel() {

    private val _roomItems = MutableStateFlow(ItemDataSource.RoomItems)
    val roomItems = _roomItems

    private val _bathroomItems = MutableStateFlow(ItemDataSource.BathroomItems)
    val bathroomItems = _bathroomItems

    val showDialog = MutableStateFlow(false)

    val selectedRoomItems = MutableStateFlow<List<MaintenanceItems>>(emptyList())

    val selectedBathroomItems = MutableStateFlow<List<MaintenanceItems>>(emptyList())

    val selectedItem = MutableStateFlow<Item?>(null)

    val selectedCategory = MutableStateFlow<String?>(null)

    val currentIssue = MutableStateFlow("")//to show the issue at dialog

    private val _maintenanceItems = MutableStateFlow<List<MaintenanceItems?>>(emptyList())
    val maintenanceItems : StateFlow<List<MaintenanceItems?>> = _maintenanceItems.asStateFlow()

    private val _maintenanceIds = MutableStateFlow<Set<Int>>(emptySet())
    val maintenanceIds : StateFlow<Set<Int>> = _maintenanceIds.asStateFlow()

    private val _issueMessage = MutableStateFlow(false)
    val issueMessage : StateFlow<Boolean> = _issueMessage.asStateFlow()

    fun openDialog(){
        showDialog.value = true
    }

    fun closeDialog(){
        showDialog.value = false
    }

    fun openIssueMessage(){
        _issueMessage.value = true
    }

    fun closeIssueMessage(){
        _issueMessage.value = false
    }

    fun resetAll(){
        selectedRoomItems.value = emptyList()
        selectedBathroomItems.value = emptyList()
        _maintenanceIds.value = emptySet()
        _maintenanceItems.value = emptyList()
        currentIssue.value = ""
        selectedItem.value = null
        selectedCategory.value = null
        closeIssueMessage()
    }

    fun onItemClick(item: Item, category: String){//here will control which item is clicked

        selectedItem.value = item
        selectedCategory.value = category

        val oldIssue = when(category){//reload the issue when item is clicked

            "Room" -> selectedRoomItems.value.find{it.id == item.itemId} ?.issue
            "Bathroom" -> selectedBathroomItems.value.find{ it.id == item.itemId } ?.issue
            else -> null

        }

        currentIssue.value = oldIssue ?: ""
        openDialog()

    }

    fun onConfirm(issue: String){
        val item = selectedItem.value ?: return
        val category = selectedCategory.value ?: return

        if(issue.isNotEmpty()){
            when(category){
                "Room" -> {

                    val newItem = MaintenanceItems(
                        category = category,
                        id = item.itemId,
                        item = item.name,
                        issue = issue
                    )
                    val currentSet = selectedRoomItems.value.toMutableList()
                    currentSet.removeAll{it.id == item.itemId}//to avoid same item but different issue is add again
                    currentSet.add(newItem)
                    selectedRoomItems.value = currentSet
                }
                "Bathroom" -> {

                    val newItem = MaintenanceItems(
                        category = category,
                        id = item.itemId,
                        item = item.name,
                        issue = issue
                    )

                    val currentSet = selectedBathroomItems.value.toMutableList()
                    currentSet.removeAll{it.id == item.itemId}//to avoid same item but different issue is add again
                    currentSet.add(newItem)
                    selectedBathroomItems.value = currentSet
                }
            }

            _maintenanceIds.value = _maintenanceIds.value + item.itemId
            closeDialog()

        }else{

            openIssueMessage()

        }

    }

    fun onDelete(id: Int){
        val room = selectedRoomItems.value.toMutableList()
        val bathroom = selectedBathroomItems.value.toMutableList()

        room.removeAll{it.id == id}
        bathroom.removeAll{it.id == id}

        selectedRoomItems.value = room
        selectedBathroomItems.value = bathroom
        _maintenanceIds.value = _maintenanceIds.value - id

        currentIssue.value = ""
        closeDialog()
    }

    fun saveMaintenanceItems(): List<MaintenanceItems>{
        _maintenanceItems.value = selectedRoomItems.value.union(selectedBathroomItems.value).toList()
        val value = _maintenanceItems.value
        return value as List<MaintenanceItems>

    }

    fun load(items: List<MaintenanceItems>){

        selectedRoomItems.value = items.filter{it.category == "Room"}
        selectedBathroomItems.value = items.filter{it.category == "Bathroom" }

        _maintenanceIds.value = items.map{it.id}.toSet()
        _maintenanceItems.value = items

    }

}