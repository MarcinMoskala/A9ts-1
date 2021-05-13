package com.a9ts.a9ts.components.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a9ts.a9ts.model.dataclass.Appointment
import com.a9ts.a9ts.tools.dateAndTimeFormatted
import com.a9ts.a9ts.ui.theme.LightGrey
import androidx.lifecycle.viewmodel.compose.viewModel
import com.a9ts.a9ts.model.dataclass.SystemPushNotification
import com.a9ts.a9ts.tools.sendSystemPushNotification

@Composable
fun Appointment(appointmentId: String, viewModel: AppointmentViewModel = viewModel()) {
    val dbInitialized = remember { mutableStateOf(false)}

    if (!dbInitialized.value) {
        viewModel.onAppointmentDetailInit(appointmentId)
        dbInitialized.value = true
    }

    val appointment: Appointment? by viewModel.appointment.observeAsState(null)

    if (appointment == null) return

    val dateAndTime = dateAndTimeFormatted(appointment!!.dateAndTime.toDate())
    val appPartnerName = viewModel.getAuthUserIdAppointmentPartnerName(appointment!!)

    Column(Modifier.padding(24.dp)) {
        Text(appPartnerName, style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(24.dp))
        Text(dateAndTime, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        val enabled: Boolean
        val onClick: () -> Unit
        val buttonText: String
        val captionText: String

        if (appointment!!.canceledByInvitee == null && appointment!!.canceledByInvitor == null) {
            enabled = true
            onClick = { viewModel.cancelAppointment(appointment!!) }
            buttonText = "Cancel appointment"
            captionText = "$appPartnerName will have to accept the cancellation, before appointment gets deleted for both of you."
        } else {
            enabled = false
            onClick = {}
            buttonText = "Cancellation request sent"
            captionText = "Waiting for $appPartnerName to accept the cancellation. After that the appointment will be deleted for both of you."
        }

        Column(Modifier.fillMaxWidth()) {
            Button(
                enabled = enabled,
                onClick = onClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            { Text(buttonText) }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                captionText,
                color = LightGrey,
                textAlign = TextAlign.Center,

                modifier = Modifier
                    .fillMaxWidth()
            )

        }
    }
}
