package com.a9ts.a9ts.components

import androidx.lifecycle.ViewModel
import com.a9ts.a9ts.ActivityViewModel
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.dataclass.SystemPushNotification
import com.a9ts.a9ts.tools.dateAndTimeFormatted
import com.a9ts.a9ts.tools.sendSystemPushNotification
import com.a9ts.a9ts.tools.toUTCTimestamp
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime

class AddAppointmentStepTwoViewModel : ViewModel(), KoinComponent {
    private val databaseService: DatabaseService by inject()
    private val authService: AuthService by inject()

    suspend fun onAddAppointmentStepTwoSubmit(friendUserId: String, localDate: LocalDate, localTime: LocalTime, activityViewModel : ActivityViewModel): Boolean {

        val returnData = databaseService.sendAppointment(
            authService.authUserId,
            friendUserId,
            toUTCTimestamp(localDate, localTime)
        )

        return if (returnData != null) {
            val (notification, authUser, friendUser) = returnData

            val dateAndTime = dateAndTimeFormatted(notification.dateAndTime!!.toDate())


            //TODO make this faster and async
            Timber.d("Before notification")
            SystemPushNotification( //askmarcin this takes 2 seconds, how to make it faster, run in background...
                title = "Appointment invitation from: ${authUser.fullName}",
                body = dateAndTime,
                token = (friendUser).deviceToken
            ).also { sendSystemPushNotification(it) }
            Timber.d("After notification")


            activityViewModel.setToast("✔ Invite sent to ${friendUser.fullName}")
            true
        } else {
            activityViewModel.setToast("❌ Invite failed. Try again...")
            false
        }

    }
}