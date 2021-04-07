package com.a9ts.a9ts.composedetail

import android.app.Application
import android.text.format.DateFormat
import android.view.View
import androidx.lifecycle.*
import com.a9ts.a9ts.R
import com.a9ts.a9ts.getMyAppointmentPartnerName
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
}