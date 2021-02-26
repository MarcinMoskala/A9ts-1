package com.a9ts.a9ts.addappointment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.Friend
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StepOneViewModel : ViewModel(), KoinComponent {
    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()

    private val _myFriends = MutableLiveData<List<Friend>>()
    val myFriends : LiveData<List<Friend>>
        get() = _myFriends

    private val _addFriendsClicked = MutableLiveData<Boolean>()
    val addFriendsClicked : LiveData<Boolean>
        get() = _addFriendsClicked



    fun onAddFriendsClickedDone() {
        _addFriendsClicked.value = false
    }

    fun onAddFriendsClicked() {
        _addFriendsClicked.value = true
    }

    init {
        viewModelScope.launch {
            _myFriends.value =  databaseService.getFriends(authService.authUserId)
        }
    }
}