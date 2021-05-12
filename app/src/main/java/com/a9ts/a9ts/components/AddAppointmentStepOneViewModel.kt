package com.a9ts.a9ts.components

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.dataclass.Friend
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddAppointmentStepOneViewModel : ViewModel(), KoinComponent{
    private val databaseService: DatabaseService by inject()
    private val authService: AuthService by inject()

    private val _addAppointmentStepOneFriends = MutableLiveData<List<Friend>>()
    val addAppointmentStepOneFriends: LiveData<List<Friend>>
        get() = _addAppointmentStepOneFriends

    fun onAddAppointmentStepOneInit() {
        viewModelScope.launch {
            _addAppointmentStepOneFriends.value = databaseService.getFriends(authService.authUserId)
        }
    }
}