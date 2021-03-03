package com.a9ts.a9ts.addappointment

import androidx.lifecycle.*
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

    val hasResults = MediatorLiveData<Boolean>().apply {
        addSource(_myFriends) {
            value = it.isNotEmpty()
        }
    }

    fun onAddFriendsClickedDone() {
        _addFriendsClicked.value = false
    }

    fun onAddFriendsClicked() {
        _addFriendsClicked.value = true
    }

    fun onCreateView() {
        viewModelScope.launch {
            _myFriends.value =  databaseService.getFriends(authService.authUserId)
        }
    }
}