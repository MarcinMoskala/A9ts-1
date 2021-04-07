package com.a9ts.a9ts.composedetail

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.a9ts.a9ts.dateAndTimeFormatted
import com.a9ts.a9ts.getMyAppointmentPartnerName
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.ui.A9tsTheme
import timber.log.Timber

class ComposeDetailFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val args = ComposeDetailFragmentArgs.fromBundle(requireArguments())
        val appointmentId = args.appointmentId
        val viewModelFactory = DetailViewModelFactory(appointmentId)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(DetailViewModel::class.java)

        viewModel.appointment.observe(viewLifecycleOwner, {
            if (it != null) {
                Timber.d("Invitee is ${it.inviteeName}")
            }
        })

        return ComposeView(requireContext()).apply {
            setContent() {
                A9tsTheme {
                    val scaffoldState = rememberScaffoldState()
                    Scaffold { // content
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
        Column(Modifier.fillMaxWidth()) {
            Button(onClick = {}, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Cancel appointment") }
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