package com.a9ts.a9ts.main

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a9ts.a9ts.model.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel : ViewModel(), KoinComponent {
    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()

    var notificationList = listOf<Notification>()
    var appointmentList = listOf<Appointment>()

    val authUserId = authService.authUserId

    private val _appointmentNotificationAccepted = MutableLiveData<BaseViewHolder?>()
    val appointmentNotificationAccepted: LiveData<BaseViewHolder?>
        get() = _appointmentNotificationAccepted

/*    private val _appointmentNotificationRejected = MutableLiveData<BaseViewHolder?>()
    val appointmentNotificationRejected: LiveData<BaseViewHolder?>
        get() = _appointmentNotificationRejected*/

    private val _friendNotificationAccepted = MutableLiveData<BaseViewHolder?>()
    val friendNotificationAccepted: LiveData<BaseViewHolder?>
        get() = _friendNotificationAccepted


    private val _friendNotificationRejected = MutableLiveData<BaseViewHolder?>()
    val friendNotificationRejected: LiveData<BaseViewHolder?>
        get() = _friendNotificationRejected
    
    
    private val _snackMessage = MutableLiveData<String?>()
    val snackMessage: LiveData<String?>
        get() = _snackMessage

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

    fun onCreateView() {
        databaseService.getNotificationsListener(authUserId) { notifications ->
            this.notificationList = notifications
            _notificationsAndAppointments.value = this.notificationList + this.appointmentList
        }

        databaseService.getAppointmentsListener(authUserId) { appointments ->
            this.appointmentList = appointments
            _notificationsAndAppointments.value = this.notificationList + this.appointmentList
        }
    }

    init {
        if (authUserId == "null") {
            _notLoggedEvent.value = Unit
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


    fun onFriendNotificationAccepted(holder: BaseViewHolder, authUserId: String?, notificationId: String?) {

        viewModelScope.launch {
            if (databaseService.acceptFriendInvite(authService.authUserId, authUserId!!, notificationId!!))
            {
                _friendNotificationAccepted.value = holder
            }
        }
    }


    fun onFriendNotificationAcceptedDone() {
        _friendNotificationAccepted.value = null
    }


    fun onFriendNotificationRejected(holder: BaseViewHolder, authUserId: String?, notificationId: String?) {
        viewModelScope.launch {
            if (databaseService.rejectFriendInvite(authService.authUserId, authUserId!!, notificationId!!))
            {
                _friendNotificationRejected.value = holder
            }
        }
    }


    fun onFriendNotificationRejectedDone() {
        _friendNotificationRejected.value = null
    }


    fun onAppointmentNotificationAccepted(holder: BaseViewHolder, invitorUserId: String?, appointmentId: String?, notificationId: String?) {
        viewModelScope.launch {
            if (databaseService.acceptAppointmentInvitation(authUserId, invitorUserId, appointmentId, notificationId)) {
                _snackMessage.value = "✔ Appointment accepted"
            }
        }
    }

    //askmarcin should this fun accept nullable strings? Or should I convert to non nullable when callling the function?
    fun onAppointmentNotificationRejected(holder: BaseViewHolder, invitorUserId: String?, appointmentId: String?, notificationId: String?) {
        viewModelScope.launch {
            if (databaseService.rejectAppointmentInvitation(authUserId, invitorUserId!!, appointmentId, notificationId)) {
                _snackMessage.value = "❌ Appointment rejected"
            }
        }
    }
   
    fun snackMessageDone() {
        _snackMessage.value = null
    }


}