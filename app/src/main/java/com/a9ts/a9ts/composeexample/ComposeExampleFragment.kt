package com.a9ts.a9ts.composeexample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import org.koin.android.ext.android.inject


val myAppointment = Appointment(
    inviteeName = "Robert Veres",
    inviteeUserId = "Os7gzVjFkyNVYVQMiPmQLZIh8Sw2",
    invitorName = "Peter Veres",
    invitorUserId = "pujbtIPGlieNOsxCTGcQVdiR5Ob2",
    state = Appointment.STATE_I_AM_INVITED
)

class ComposeExampleFragment : Fragment() {
    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppointmentBox(
                    myAppointment, authService.authUserId
                )
            }
        }
    }
}

@Composable
fun AppointmentBox(appointment: Appointment, authUserId: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(155, 255, 255))
            .padding(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("${appointment.inviteeName}")
        }
    }
}

@Preview
@Composable
fun AppointmentBoxPreview() {
    AppointmentBox(
        appointment = myAppointment,
        authUserId = "Os7gzVjFkyNVYVQMiPmQLZIh8Sw2" //Robert Veres
    )
}
