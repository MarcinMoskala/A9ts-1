package com.a9ts.a9ts.addappointment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StepOneViewModel : ViewModel(), KoinComponent {
    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()

    private val _myFriends = MutableLiveData<List<User>>()
    val myFriends : LiveData<List<User>>
        get() = _myFriends



    init {
        viewModelScope.launch(Dispatchers.IO){
            _myFriends.value =  databaseService.getFriends(authService.authUserId)
        }
    }
}