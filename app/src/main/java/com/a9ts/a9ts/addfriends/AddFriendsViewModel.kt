package com.a9ts.a9ts.addfriends

import androidx.lifecycle.*
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.Friend
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class AddFriendsViewModel : ViewModel(), KoinComponent {

    private val databaseService: DatabaseService by inject()
    private val authService: AuthService by inject()

    private val _newFriendsList = MutableLiveData<List<Friend>>()
    val newFriendsList: LiveData<List<Friend>>
        get() = _newFriendsList


    private val _buttonClicked = MutableLiveData<Pair<Boolean?, Int>>()
    val buttonClicked: LiveData<Pair<Boolean?, Int>>
        get() = _buttonClicked


    val hasResults = MediatorLiveData<Boolean>().apply {
        addSource(_newFriendsList) {
            value = it.isNotEmpty()
        }
    }

    fun onButtonClicked(userId : String, viewHolderPosition : Int) {
//        viewModelScope.launch {
//            _buttonClicked.value = Pair(databaseService.sendFriendInvite(authService.authUserId, userId), viewHolderPosition)
//        }
    }

    fun onTextChanged(s: CharSequence?) {
        viewModelScope.launch {
            _newFriendsList.value = databaseService.getNonFriends(s.toString(), authService.authUserId)
        }
    }

    // not nice... don't know how to do it...
    fun onButtonClickedDone() {
        _buttonClicked.value = Pair(null, -1)
    }
}
