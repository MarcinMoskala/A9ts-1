package com.a9ts.a9ts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.a9ts.a9ts.ComposeViewModel
import com.a9ts.a9ts.dateFormatted
import com.a9ts.a9ts.getMyAppointmentPartnerName
import com.a9ts.a9ts.main.MainFragmentDirections
import com.a9ts.a9ts.main.MainFragmentViewModel
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.model.Notification
import com.a9ts.a9ts.model.mockAppointmentNotification
import com.a9ts.a9ts.timeFormatted
import com.a9ts.a9ts.ui.A9tsTheme
import com.a9ts.a9ts.ui.BgGrey
import com.a9ts.a9ts.ui.LightGrey
import com.a9ts.a9ts.ui.Shapes
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController

@Composable
fun MainComponent(viewModel: ComposeViewModel, navHostController: NavHostController, snackbarHostState: SnackbarHostState, authUserId: String) {
    viewModel.onInitMain() // askmarcin, this should be done differently I think
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(8.dp)) {
            NotificationsList(viewModel, snackbarHostState)
            AppointmentsList(viewModel.appointmentList, authUserId, navHostController)
        }
    }
}

@Composable
fun NotificationBox(
    notification: Notification,
    onAccept: () -> Unit,
    onReject: () -> Unit = {},
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
                Button(onClick = { onReject() }) { Text(rejectText) }
            }
        }
    }
}

@Composable
fun AppointmentBox(appointment: Appointment, authUserId: String, navController: NavController) {
    BlackLine()
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable {
                navController.navigate(MainFragmentDirections.actionMainFragmentToDetailFragment(appointment.id!!))
            }
    ) {
        val date = appointment.dateAndTime.toDate()

        val appointmentPartnerName = getMyAppointmentPartnerName(authUserId, appointment.invitorUserId, appointment.invitorName, appointment.inviteeName)

        if (appointment.state == Appointment.STATE_I_INVITED) {
            AppointmentWaitingToBeAcceptedRow()
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
fun BlackLine() {
    Divider(color = Color.Black, thickness = 1.dp)
}

@Composable
fun AppointmentWaitingToBeAcceptedRow() {
    AppointmentStateRow(LightGrey, "Waiting to be accepted ...")
}

@Composable
fun AppointmentStateRow(bgColor: Color, text: String) {
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
fun AppointmentsList(appointmentList: LiveData<List<Appointment>>, authUserId: String, navController: NavController) {
    val appointmentList: List<Appointment> by appointmentList.observeAsState(listOf())

    LazyColumn {
        items(appointmentList) { appointment ->
            AppointmentBox(appointment, authUserId, navController)
        }
    }

    if (appointmentList.isNotEmpty()) BlackLine()
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
                        scope.launch {
                            snackbarHostState.showSnackbar(message = "✔ Appointment accepted.")
                        }
                    }, onReject = {
                        viewModel.onAppointmentNotificationRejected(
                            invitorUserId = notification.authUserId,
                            appointmentId = notification.appointmentId,
                            notificationId = notification.id!!
                        )
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
    A9tsTheme() {
        Scaffold(backgroundColor = BgGrey) {
            Column {
                NotificationBox(mockAppointmentNotification, onAccept = {}, onReject = {}, "Agree!", "I can't")
                NotificationBox(mockAppointmentNotification, onAccept = {}, onReject = {}, "Agree!", "I can't")
            }
        }
    }
}