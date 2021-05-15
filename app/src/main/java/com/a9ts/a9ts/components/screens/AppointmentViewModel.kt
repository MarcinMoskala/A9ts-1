package com.a9ts.a9ts.components.screens

import androidx.lifecycle.*
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.dataclass.Appointment
import com.a9ts.a9ts.model.dataclass.SystemPushNotification
import com.a9ts.a9ts.tools.dateAndTimeFormatted
import com.a9ts.a9ts.tools.getMyIdAppointmentPartnerName
import com.a9ts.a9ts.tools.sendSystemPushNotification
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class AppointmentViewModel(appointmentId: String) : ViewModel(), KoinComponent {

    private val databaseService: DatabaseService by inject()
    private val authService: AuthService by inject()

    private val _appointment = MutableLiveData<Appointment>()
    val appointment: LiveData<Appointment>
        get() = _appointment

    init {
        databaseService.getAppointmentListener(appointmentId, authService.authUserId) { appointment ->
            _appointment.value = appointment
        }
    }

    fun cancelAppointment(appointment: Appointment) {
        viewModelScope.launch {
            // sends System Notification Too
            if (databaseService.cancelAppointmentRequest(authService.authUserId, appointment))
            {
                // TODO UI feedback
            } else {

            }
        }
    }

    fun getAuthUserIdAppointmentPartnerName(appointment: Appointment): String {
        return getMyIdAppointmentPartnerName(authService.authUserId, appointment)
    }
}

class AppointmentViewModelFactory(private val appointmentId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentViewModel::class.java)) {
            return AppointmentViewModel(appointmentId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}