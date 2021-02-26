package com.a9ts.a9ts.main

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.User
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel : ViewModel(), KoinComponent {
    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()


    val authUserId = authService.authUserId


    private val _friendNotificationAccepted = MutableLiveData<Int?>()
    val friendNotificationAccepted: LiveData<Int?>
        get() = _friendNotificationAccepted


    private val _notLoggedEvent = MutableLiveData<Unit>()
    val notLoggedEvent: LiveData<Unit>
        get() = _notLoggedEvent


    private val _showUser = MutableLiveData<User>()
    val showUser: LiveData<User>
        get() = _showUser

    private val _fabClicked = MutableLiveData<Boolean>()
    val fabClicked: LiveData<Boolean>
        get() = _fabClicked

    private val _notificationsAndAppointments = MutableLiveData<List<Any>>()
    val notificationsAndAppointments : LiveData<List<Any>>
        get() = _notificationsAndAppointments

    fun fabClicked(view: View) {
        _fabClicked.value = true
    }

    fun fabClickedDone() {
        _fabClicked.value = false
    }

    fun showUserDone() {
        _showUser.value = null
    }


    init {
        if (authUserId == "null") {
            _notLoggedEvent.value = Unit
        }

        viewModelScope.launch {
            _notificationsAndAppointments.value =  databaseService.getNotificationsAndAppointments(authUserId)
        }
    }


    fun onMenuAbout() {
        viewModelScope.launch {
            val user = databaseService.getUser(authService.authUserId)

            if (user != null) {
                _showUser.value = user
            } else if (authService.authUserId != "null") {
                _showUser.value = User(authService.authUserId, "No name", authService.getPhoneNumber())
            }
        }
    }


    fun onFriendNotificationAccepted(itemPosition: Int, authUserId: String?) {
        viewModelScope.launch {
            if (databaseService.acceptFriendInvite(authService.authUserId, authUserId!!))
            {
                _friendNotificationAccepted.value = itemPosition
            }
        }
    }

    fun onFriendNotificationAcceptedDone() {
        _friendNotificationAccepted.value = null
    }



    fun onFriendNotificationRejected(itemPosition: Int, authUserId: String?) {
        TODO("Not yet implemented")
    }
}