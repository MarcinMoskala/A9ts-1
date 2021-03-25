package com.a9ts.a9ts.detail

import android.app.Application
import android.text.format.DateFormat
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.a9ts.a9ts.R
import com.a9ts.a9ts.getMyAppointmentPartnerName
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DetailViewModel(val appointment: Appointment, app: Application) : AndroidViewModel(app), KoinComponent {

    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()

    private val _appointmentPartnerName = MutableLiveData<String>()
    val appointmentPartnerName: LiveData<String>
        get() = _appointmentPartnerName





    private val _cancelButtonDescription = MutableLiveData<String>()
    val cancelButtonDescription: LiveData<String>
        get() = _cancelButtonDescription

    private val _onCancelAppointmentRequestFailed = MutableLiveData<Boolean>()
    val onCancelInvitatonFailed: LiveData<Boolean>
        get() = _onCancelAppointmentRequestFailed

    private val _cancelButtonClicked = MutableLiveData<Unit>()
    val cancelButtonClicked: LiveData<Unit>
        get() = _cancelButtonClicked


    private val _iCanInviteLayoutVisibility = MutableLiveData<Int>()
    val iCanInviteLayoutVisibility: LiveData<Int>
        get() = _iCanInviteLayoutVisibility

    private val _dateAndTime = MutableLiveData<String>()
    val dateAndTime: LiveData<String>
        get() = _dateAndTime


    fun onCancelButtonClicked() {
        _cancelButtonClicked.value = Unit

        viewModelScope.launch {
            _onCancelAppointmentRequestFailed.value = databaseService.cancelAppointmentRequest(
                invitorIsCanceling = authService.authUserId == appointment.invitorUserId,
                invitorId = appointment.invitorUserId,
                inviteeId = appointment.inviteeUserId,
                appointmentId = appointment.id!!
            ) == false
        }
    }

    init {
        _appointmentPartnerName.value = getMyAppointmentPartnerName(
            authUserID = authService.authUserId,
            invitorUserId = appointment.invitorUserId,
            inviteeFullName = appointment.inviteeName,
            invitorFullName = appointment.invitorName
        )

        // no cancelation yet
        if (appointment.canceledByInvitee == null && appointment.canceledByInvitor == null) {
            _iCanInviteLayoutVisibility.value = View.VISIBLE
            this._cancelButtonDescription.value = app.getString(R.string.cancel_button_description, appointmentPartnerName.value?.substringBefore(' '))
        }

        if (appointment.canceledByInvitee != null && appointment.canceledByInvitor != null) {
            throw RuntimeException("inviteeCanced not null AND invitorCanceled not null in Appointment.") // Force a crash to test Crashlytics
        } else {
            // canceled by me
            if ((appointment.invitorUserId == authService.authUserId && appointment.canceledByInvitor != null) ||
                (appointment.inviteeUserId == authService.authUserId && appointment.canceledByInvitee != null)
            ) {
            }


            // canceled only by him


        }


        // I requested cancelation


        val date = appointment.dateAndTime.toDate()
        _dateAndTime.value = DateFormat.format("E dd LLL", date).toString() + " at " + DateFormat.format("HH:mm", date).toString()


    }
}