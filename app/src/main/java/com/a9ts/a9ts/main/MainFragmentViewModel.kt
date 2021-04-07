package com.a9ts.a9ts.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.Notification
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class MainFragmentViewModel : ViewModel(), KoinComponent {
    private var _aboutUser = MutableLiveData<String?>()
    val aboutUser: LiveData<String?>
        get() = _aboutUser

    private var _appointmentList = MutableLiveData<List<Appointment>>(listOf())
    val appointmentList: LiveData<List<Appointment>>
        get() = _appointmentList

    private var _notificationList = MutableLiveData<List<Notification>>(listOf())
    val notificationList: LiveData<List<Notification>>
        get() = _notificationList

    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()

    fun getUserId(): String {
        return authService.authUserId
    }

    init {
            databaseService.getAppointmentsListener(authService.authUserId) { appointmentList ->
                _appointmentList.value = appointmentList
            }

            databaseService.getNotificationsListener(authService.authUserId) { notificationList ->
                _notificationList.value = notificationList
            }
    }

    fun onAppointmentNotificationAccepted(invitorUserId: String, appointmentId: String, notificationId: String) {
        viewModelScope.launch {
            if (databaseService.acceptAppointmentInvitation(authService.authUserId, invitorUserId, appointmentId, notificationId)) {
                Timber.d("✔ Appointment accepted.")
            }
        }
    }

    fun onAppointmentNotificationRejected(invitorUserId: String, appointmentId: String, notificationId: String) {
        viewModelScope.launch {
            if (databaseService.rejectAppointmentInvitation(authService.authUserId, invitorUserId, appointmentId, notificationId)) {
// TODO         _snackMessage.value = "❌ Appointment rejected"
                Timber.d("❌ Appointment rejected.")
            }
        }
    }

    fun onFriendNotificationAccepted(authUserId: String, notificationId: String) {

        viewModelScope.launch {
            if (databaseService.acceptFriendInvite(authService.authUserId, authUserId, notificationId)) {
                Timber.d("✔ Friendship accepted.")
            }
        }
    }

    fun onFriendNotificationRejected(authUserId: String, notificationId: String) {
        viewModelScope.launch {
            if (databaseService.rejectFriendInvite(authService.authUserId, authUserId, notificationId)) {
                Timber.d("❌ Friendsih request rejected.")
            }
        }
    }

    fun onAboutUser() {
        viewModelScope.launch {
            _aboutUser.value = databaseService.getUser(authService.authUserId)?.fullName
        }
    }

    fun onAboutUserShowed() {
        _aboutUser.value = null
    }
}