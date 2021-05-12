package com.a9ts.a9ts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import com.a9ts.a9ts.ComposeViewModel
import com.a9ts.a9ts.tools.dateFormatted
import com.a9ts.a9ts.tools.getMyIdAppointmentPartnerName
import com.a9ts.a9ts.model.dataclass.Appointment
import com.a9ts.a9ts.model.dataclass.Notification
import com.a9ts.a9ts.model.mockAppointmentNotification
import com.a9ts.a9ts.tools.timeFormatted
import com.a9ts.a9ts.ui.theme.A9tsTheme
import com.a9ts.a9ts.ui.theme.BgGrey
import com.a9ts.a9ts.ui.theme.LightGrey
import com.a9ts.a9ts.ui.theme.Shapes
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun Main(viewModel: ComposeViewModel, navHostController: NavHostController, snackbarHostState: SnackbarHostState, authUserId: String) {
    val dbInitialized = remember { mutableStateOf(false) }
    // askmarcin - not sure if
    // "val dbInitialized = remember { mutableStateOf(false) }" or
    // "var dbInitialized by remember { mutableStateOf(false) }" is considered better?

    if (!dbInitialized.value) {
        Timber.d("dbInitialize: false... Initializing DB")
        viewModel.onMainInit() // askmarcin, this should be done differently I think
        dbInitialized.value = true
    }

    Column(modifier = Modifier
        .padding(8.dp)
        .fillMaxSize()) {
        NotificationsList(viewModel, snackbarHostState)
        AppointmentsList(viewModel, authUserId, navHostController)
    }
}

@Composable
fun NotificationBox(
    notification: Notification,
    onAccept: () -> Unit,
    onReject: (() -> Unit)? = null,
    acceptText: String,
    rejectText: String = ""
) {
    Surface(
        color = Color.White,
        elevation = 6.dp,
        shape = Shapes.small,
        modifier = Modifier.padding(8.dp)

    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            when (notification.notificationType) {
                Notification.TYPE_APP_INVITATION -> {
                    Text("Appointment with ${notification.fullName}", Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp))
                    Text(
                        "${notification.dateAndTime!!.toDate().dateFormatted()} ${notification.dateAndTime!!.toDate().timeFormatted()}",
                        Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp)
                    )
                }

                Notification.TYPE_FRIEND_INVITATION -> {
                    Text("Friend invitation from ${notification.fullName}", Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp))
                }

                Notification.TYPE_CANCELLATION -> {
                    Text("${notification.fullName} cancelled your appointment.", Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp))
                    Text(
                        "${notification.dateAndTime!!.toDate().dateFormatted()} ${notification.dateAndTime!!.toDate().timeFormatted()}",
                        Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp)
                    )
                }

            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween)
            {
                Button(onClick = { onAccept() }) { Text(acceptText) }

                if (onReject != null) {
                    Button(onClick = { onReject() }) { Text(rejectText) }
                }
            }
        }
    }
}

@Composable
fun AppointmentRow(appointment: Appointment, authUserId: String, navHostController: NavHostController) {
    BlackLine()
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable {
                navHostController.navigate("appointment/${appointment.id}")
                // TODO: navigate to detail
                // navHostController.navigate(MainFragmentDirections.actionMainFragmentToDetailFragment(appointment.id!!))
            }
    ) {
        val date = appointment.dateAndTime.toDate()

        val appointmentPartnerName = getMyIdAppointmentPartnerName(authUserId, appointment.invitorUserId, appointment.invitorName, appointment.inviteeName)

        if (appointment.state == Appointment.STATE_I_INVITED) {
            AppointmentWaitingToBeAcceptedTag()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        {
            Column(Modifier.width(90.dp)) {
                Text(date.dateFormatted(), fontWeight = FontWeight.Bold)
                Text(
                    date.timeFormatted(),
                    color = LightGrey,
                    modifier = Modifier.padding(start = 0.dp, top = 8.dp, end = 0.dp, bottom = 0.dp)
                )
            }
            Column { Text(appointmentPartnerName, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun AppointmentWaitingToBeAcceptedTag() {
    AppointmentStateTag(LightGrey, "Waiting to be accepted ...")
}

@Composable
fun AppointmentStateTag(bgColor: Color, text: String) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 0.dp)
            .background(bgColor),

        ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier
                .padding(4.dp)
        )
    }
}

@Composable
fun AppointmentsList(viewModel: ComposeViewModel, authUserId: String, navHostController: NavHostController) {
    val appointments: List<Appointment> by viewModel.appointmentList.observeAsState(listOf())

    LazyColumn {
        items(appointments) { appointment ->
            AppointmentRow(appointment, authUserId, navHostController)
        }
    }

    if (appointments.isNotEmpty()) BlackLine()
}


@Composable
fun NotificationsList(viewModel: ComposeViewModel, snackbarHostState: SnackbarHostState) {
    val notificationList: List<Notification> by viewModel.notificationList.observeAsState(listOf())
    val scope = rememberCoroutineScope()

    LazyColumn(Modifier.padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 8.dp)) {
        items(notificationList) { notification ->
            when (notification.notificationType) {
                Notification.TYPE_APP_INVITATION -> NotificationBox(
                    notification = notification,
                    onAccept = {
                        viewModel.onAppointmentNotificationAccepted(
                            invitorUserId = notification.authUserId,
                            appointmentId = notification.appointmentId,
                            notificationId = notification.id!!
                        )
                        // TODO only if success
                        scope.launch {
                            snackbarHostState.showSnackbar(message = "✔ Appointment accepted.")
                        }
                    }, onReject = {
                        viewModel.onAppointmentNotificationRejected(
                            invitorUserId = notification.authUserId,
                            appointmentId = notification.appointmentId,
                            notificationId = notification.id!!
                        )

                        // TODO only if success
                        scope.launch {
                            snackbarHostState.showSnackbar(message = "❌ Appointment rejected.")
                        }

                    }, acceptText = "Agree!", rejectText = "I can't"
                )

                Notification.TYPE_FRIEND_INVITATION -> NotificationBox(
                    notification = notification,
                    onAccept = {
                        viewModel.onFriendNotificationAccepted(
                            authUserId = notification.authUserId,
                            notificationId = notification.id!!
                        )
                    }, onReject = {
                        viewModel.onFriendNotificationRejected(
                            authUserId = notification.authUserId,
                            notificationId = notification.id!!
                        )
                    }, acceptText = "Add friend", rejectText = "Reject"
                )

                Notification.TYPE_CANCELLATION -> NotificationBox(
                    notification = notification,
                    onAccept = {
                        viewModel.onCancellationAccepted(
                            appPartnerId = notification.authUserId,
                            appointmentId = notification.appointmentId,
                            notificationId = notification.id!!
                        )
                    },
                    acceptText = "Ok"
                )
            }
        }
    }
}


@Preview
@Composable
fun PreviewNotificationBox() {
    A9tsTheme {
        Scaffold(backgroundColor = BgGrey) {
            Column {
                NotificationBox(mockAppointmentNotification, onAccept = {}, onReject = {}, "Agree!", "I can't")
                NotificationBox(mockAppointmentNotification, onAccept = {}, onReject = {}, "Agree!", "I can't")
            }
        }
    }
}