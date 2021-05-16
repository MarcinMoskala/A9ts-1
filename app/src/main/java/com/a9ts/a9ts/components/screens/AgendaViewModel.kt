package com.a9ts.a9ts.components.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.material.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import com.a9ts.a9ts.ActivityViewModel
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.dataclass.Appointment
import com.a9ts.a9ts.model.dataclass.Notification
import com.a9ts.a9ts.tools.Route
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class AgendaViewModel : ViewModel(), KoinComponent {
    private val databaseService: DatabaseService by inject()
    private val authService: AuthService by inject()

    private var _appointmentList = MutableLiveData<List<Appointment>>(listOf())
    val appointmentList: LiveData<List<Appointment>>
        get() = _appointmentList

    private var _notificationList = MutableLiveData<List<Notification>>(listOf())
    val notificationList: LiveData<List<Notification>>
        get() = _notificationList

    private var _fullName = MutableLiveData("")
    val fullName: LiveData<String>
        get() = _fullName

    init {
        Timber.d("init {} called")
        viewModelScope.launch {
            databaseService.getAppointmentsListener(authService.authUserId) { appointmentList ->
                _appointmentList.value = appointmentList
            }

            databaseService.getNotificationsListener(authService.authUserId) { notificationList ->
                _notificationList.value = notificationList
            }

            _fullName.value = databaseService.getUser(authService.authUserId)?.fullName
        }
    }

    fun onShowDeviceToken(snackbarHostState: SnackbarHostState) {
        viewModelScope.launch {
            val deviceToken = FirebaseMessaging.getInstance().token.await()
            snackbarHostState.showSnackbar(deviceToken)
        }
    }

    fun onLogout(navHostController: NavHostController, snackbarHostState: SnackbarHostState) {
        authService.signOut()
        navHostController.navigate(Route.AUTH_STEP_ONE)

        viewModelScope.launch {
            snackbarHostState.showSnackbar("You were logged out.")
        }
    }

    fun onAppointmentNotificationAccepted(
        invitorUserId: String,
        appointmentId: String,
        notificationId: String,
        snackbarHostState: SnackbarHostState
    ) {
        viewModelScope.launch {
            if (databaseService.acceptAppointmentInvitation(authService.authUserId, invitorUserId, appointmentId, notificationId)) {
                snackbarHostState.showSnackbar("✔ Appointment accepted.")
            }
        }
    }

    fun onAppointmentNotificationRejected(
        invitorUserId: String,
        appointmentId: String,
        notificationId: String,
        snackbarHostState: SnackbarHostState
    ) {
        viewModelScope.launch {
            if (databaseService.rejectAppointmentInvitation(authService.authUserId, invitorUserId, appointmentId, notificationId)) {
                snackbarHostState.showSnackbar(message = "❌ Appointment rejected.")
            }
        }
    }


    fun onFriendNotificationAccepted(
        authUserId: String,
        notificationId: String,
        snackbarHostState: SnackbarHostState
    ) {
        viewModelScope.launch {
            if (databaseService.acceptFriendInvite(authService.authUserId, authUserId, notificationId)) {
                snackbarHostState.showSnackbar(message = "✔ Friendship accepted.")
            }
        }
    }


    fun onFriendNotificationRejected(
        authUserId: String,
        notificationId: String,
        snackbarHostState: SnackbarHostState
    ) {
        viewModelScope.launch {
            if (databaseService.rejectFriendInvite(authService.authUserId, authUserId, notificationId)) {
                snackbarHostState.showSnackbar(message = "❌ Friendship request rejected.")
            }
        }
    }


    fun onCancellationAccepted(
        appPartnerId: String,
        appointmentId: String,
        notificationId: String,
        snackbarHostState: SnackbarHostState
    ) {
        viewModelScope.launch {
            if (databaseService.acceptAppointmentCancellation(authService.authUserId, appPartnerId, appointmentId, notificationId)) {
                snackbarHostState.showSnackbar(message = "Cancellation accepted.")
            }
        }
    }
}