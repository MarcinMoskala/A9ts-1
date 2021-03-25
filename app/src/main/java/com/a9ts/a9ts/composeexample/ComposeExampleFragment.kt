package com.a9ts.a9ts.composeexample

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a9ts.a9ts.getMyAppointmentPartnerName
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.example.jatpackcomposebasics.ui.theme.A9tsTheme
import com.example.jatpackcomposebasics.ui.theme.LightGrey
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


val myAppointment = Appointment(
    inviteeName = "Robert Veres",
    inviteeUserId = "Os7gzVjFkyNVYVQMiPmQLZIh8Sw2",
    invitorName = "Peter Veres",
    invitorUserId = "pujbtIPGlieNOsxCTGcQVdiR5Ob2",
    state = Appointment.STATE_I_INVITED
)

class ComposeExampleFragment : Fragment() {

    val viewModel by viewModels<ComposableExampleFragmentViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                A9tsTheme {
                    Column(modifier = Modifier.padding(8.dp)) {
                        AppointmentsList(viewModel)
                    }
                }
            }
        }
    }

    class ComposableExampleFragmentViewModel : ViewModel(), KoinComponent {
        private var _appointmentList = MutableLiveData<List<Appointment>>(listOf())
        val appointmentList: LiveData<List<Appointment>>
            get() = _appointmentList

        private val authService: AuthService by inject()
        private val databaseService: DatabaseService by inject()

        fun getUserId(): String {
            return authService.authUserId
        }

        init {
            viewModelScope.launch {
                databaseService.getAppointmentsListener(authService.authUserId) { appointmentList ->
                    _appointmentList.value = appointmentList
                }
            }
        }
    }


    @Composable
    fun AppointmentBox(
        appointment: Appointment,
        authUserId: String
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            val date = appointment.dateAndTime.toDate()
            val dateFormatted = DateFormat.format("E dd LLL", date).toString()
            val timeFormatted = DateFormat.format("HH:mm", date).toString()

            val appointmentPartnerName = getMyAppointmentPartnerName(authUserId, appointment.invitorUserId, appointment.invitorName, appointment.inviteeName)

            BlackLine()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(255, 255, 255))
                    .padding(8.dp)
            ) {

                if (appointment.state == Appointment.STATE_I_INVITED) {
                    Text("Waiting to accept...")
                }

                Column(Modifier.width(90.dp)) {
                    Text(dateFormatted, fontWeight = FontWeight.Bold)
                    Text(timeFormatted, color = LightGrey,
                        modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 0.dp))
                }
                Column() {
                    Text(appointmentPartnerName, fontWeight = FontWeight.Bold)
                }
            }

        }
    }

    @Composable
    fun BlackLine() {
        Divider(color = Color.Black, thickness = 1.dp)
    }

    @Preview
    @Composable
    fun AppointmentBoxPreview() {
        A9tsTheme {
            Column(modifier = Modifier.padding(8.dp)) {
                AppointmentBox(
                    appointment = myAppointment,
                    authUserId = "Os7gzVjFkyNVYVQMiPmQLZIh8Sw2" //Robert Veres
                )
                AppointmentBox(
                    appointment = myAppointment,
                    authUserId = "Os7gzVjFkyNVYVQMiPmQLZIh8Sw2" //Robert Veres
                )
                BlackLine()
            }
        }
    }

    @Composable
    fun AppointmentsList(viewModel: ComposableExampleFragmentViewModel) {
        val appointmentList: List<Appointment> by viewModel.appointmentList.observeAsState(listOf())

        LazyColumn {
            items(appointmentList) { myAppointment ->
                AppointmentBox(myAppointment, viewModel.getUserId())
            }
        }

        if (appointmentList.isNotEmpty()) BlackLine()
    }
}


