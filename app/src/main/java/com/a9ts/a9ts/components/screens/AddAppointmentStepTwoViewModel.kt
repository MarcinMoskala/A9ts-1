package com.a9ts.a9ts.components.screens

import androidx.lifecycle.ViewModel
import com.a9ts.a9ts.ActivityViewModel
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.tools.toUTCTimestamp
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.time.LocalTime

class AddAppointmentStepTwoViewModel : ViewModel(), KoinComponent {
    private val databaseService: DatabaseService by inject()
    private val authService: AuthService by inject()

    suspend fun onAddAppointmentStepTwoSubmit(friendUserId: String, localDate: LocalDate, localTime: LocalTime, activityViewModel : ActivityViewModel): Boolean {
        if (databaseService.sendAppointment(authService.authUserId, friendUserId, toUTCTimestamp(localDate, localTime))) {
            activityViewModel.setToast("✔ Invite sent.")
            return true
        } else {
            activityViewModel.setToast("❌ Invite failed. Try again...")
            return false
        }

    }
}




