package com.a9ts.a9ts.detail

import androidx.lifecycle.*
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DetailViewModel(appointmentId: String) : ViewModel(), KoinComponent {


    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()

    private val _appointment = MutableLiveData<Appointment>()
    val appointment: LiveData<Appointment>
        get() = _appointment

    val authUserId = authService.authUserId //askmarcin I want to be able to have this in any fragment... shoud I have a viewModel for my Activity?

    init {
        databaseService.getAppointmentListener(appointmentId, authUserId) { appointment ->
            _appointment.value = appointment
        }
    }

    fun cancelAppointment(authUserId: String, invitorId:String, inviteeId:String, appointmentId: String) {
        viewModelScope.launch {
            databaseService.cancelAppointmentRequest(authUserId, invitorId, inviteeId, appointmentId)
            //TODO if toto je false tak nastav Toast<Livedata<String>> na nieco
        }
    }

}