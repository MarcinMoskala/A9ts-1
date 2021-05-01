package com.a9ts.a9ts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
fun AddAppointmentStepOne(viewModel: ComposeViewModel, navHostController: NavHostController, snackbarHostState: SnackbarHostState, authUserId: String) {
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
                    Column(Modifier.background(Color.White)) {
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
                                modifier = Modifier.alpha(if (friend.state == Friend.STATE_I_INVITED) 1f else 0f )
                            ) {
                                Text("Invited")
                            }
                        }
                    }
                }
            }
            BlackLine()
            Spacer(Modifier.height(16.dp))
        }

        Button(onClick = {navHostController.navigate("add_friend")}) {
            Text("Invite friend to Obvio")
        }
    }

}

