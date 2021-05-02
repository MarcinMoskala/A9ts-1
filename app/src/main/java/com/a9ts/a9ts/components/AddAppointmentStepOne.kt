package com.a9ts.a9ts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import com.a9ts.a9ts.ComposeViewModel
import com.a9ts.a9ts.model.Friend
import timber.log.Timber


@Composable
fun AddAppointmentStepOne(viewModel: ComposeViewModel, navHostController: NavHostController, snackbarHostState: SnackbarHostState) {
    val dbInitialized = remember { mutableStateOf(false) }
    val friends: List<Friend> by viewModel.addAppointmentStepOneFriends.observeAsState(listOf())

    if (!dbInitialized.value) {
        Timber.d("dbInitialize: false... Initializing DB")
        viewModel.onAddAppointmentStepOneInit()
        dbInitialized.value = true
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        if (friends.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(friends) { friend ->
                    FriendRow(friend, navHostController)
                }
            }
            BlackLine()
            Spacer(Modifier.height(16.dp))
        }

        Button(onClick = { navHostController.navigate("addFriend") }) {
            Text("Invite friend to Obvio")
        }
    }
}

@Composable
private fun FriendRow(friend: Friend, navHostController: NavHostController) {
    val shouldShowDialog = remember { mutableStateOf(false) }

    if (shouldShowDialog.value) {
        InvitationNotYetAcceptedDialog(friend.fullName, shouldShowDialog)
    }

    Column(
        Modifier
            .background(Color.White)
            .clickable {
                if (friend.state == Friend.STATE_I_INVITED) {
                    // TODO: setStateToShowAlert
                    shouldShowDialog.value = true
                } else {
                    navHostController.navigate("addAppointmentStepTwo/${friend.fullName}/${friend.authUserId}")

                }
            }) {
        BlackLine()
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                friend.fullName,
                fontWeight = FontWeight.Bold,
            )
            Button(
                onClick = {},
                enabled = false,
                // bit of a hack, basicly doing visibility = gone
                modifier = Modifier.alpha(if (friend.state == Friend.STATE_I_INVITED) 1f else 0f)
            ) {
                Text("Invited")
            }
        }
    }
}

@Composable
fun InvitationNotYetAcceptedDialog(fullName: String, shouldShowDialog: MutableState<Boolean>) {
    if (shouldShowDialog.value) {
        AlertDialog(
            onDismissRequest = { shouldShowDialog.value = false },
            title = { Text("Can't create appointment)") },
            text = { Text("$fullName hasn't accepted your friend invitation yet.") },

            confirmButton = {
                Button(
                    onClick = { shouldShowDialog.value = false }
                ) {
                    Text("Okey", color = Color.White)
                }
            }
        )
    }
}

