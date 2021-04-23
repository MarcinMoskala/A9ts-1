package com.a9ts.a9ts.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.a9ts.a9ts.dateAndTimeFormatted
import com.a9ts.a9ts.getMyAppointmentPartnerName
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.ui.A9tsTheme
import com.a9ts.a9ts.ui.LightGrey
import timber.log.Timber

class DetailFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val args = DetailFragmentArgs.fromBundle(requireArguments())
        val appointmentId = args.appointmentId
        val viewModelFactory = DetailViewModelFactory(appointmentId)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(DetailViewModel::class.java)

        return ComposeView(requireContext()).apply {

            setContent() {
                val scaffoldState = rememberScaffoldState()

                A9tsTheme {
                    Scaffold (
                        scaffoldState = scaffoldState
                    ) { // content
                        // askmarcin where should I put the authUserId? I would like to have it accessible in the whole Activity... not sure if it's ok
                        AppointmentDetail(viewModel, viewModel.authUserId)
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentDetail(viewModel: DetailViewModel, authUserId: String) {
    val appointment: Appointment? by viewModel.appointment.observeAsState(null)

    if (appointment == null) return

    val dateAndTime = dateAndTimeFormatted(appointment!!.dateAndTime.toDate())
    val appPartnerName = getMyAppointmentPartnerName(authUserId, appointment!!)
    Column(Modifier.padding(24.dp)) {
        Text(appPartnerName, style = MaterialTheme.typography.h1)
        Spacer(modifier = Modifier.height(24.dp))
        Text(dateAndTime, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // TODO poriesit stav a text podla cancelation state

        val enabled: Boolean
        val onClick: () -> Unit
        val buttonText: String
        val captionText: String

        if (appointment!!.canceledByInvitee == null && appointment!!.canceledByInvitor == null) {
            enabled = true
            onClick = { viewModel.cancelAppointment(authUserId, appointment!!) }
            buttonText = "Cancel appointment"
            captionText = "$appPartnerName will have to accept the cancellation, before appointment gets deleted for both of you."
        } else {
            enabled = false
            onClick = {}
            buttonText = "Cancellation request sent"
            captionText = "Waiting for $appPartnerName to accept the cancelation. After that the appointment will be deleted for both of you."
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

@Preview
@Composable
fun HelloScreen() {
    Column {
        var name by rememberSaveable { mutableStateOf("") }
        Text(text = "Hello, ${name}")
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") }
        )
    }
}

//@Composable
//@Preview
//fun PreviewAppointmentDetail() {
//    AppointmentDetail("Fri 02 Apr at 23:00", "Barack Obama")
//}