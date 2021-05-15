package com.a9ts.a9ts.components.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.a9ts.a9ts.model.dataclass.Friend
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.a9ts.a9ts.components.BlackLine
import com.a9ts.a9ts.components.MyTopBar
import com.a9ts.a9ts.ui.theme.BgGrey


@Composable
fun AddFriend(scaffoldState: ScaffoldState, viewModel: AddFriendViewModel = viewModel()) {
    val search = remember { mutableStateOf("") }
    val friends: List<Friend> by viewModel.addFriendsList.observeAsState(listOf())

    Scaffold(
        backgroundColor = BgGrey,
        scaffoldState = scaffoldState,
        topBar = { MyTopBar(title = "Invite friends") },
    ) {

        Column(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box {
                Box(
                    Modifier
                        .matchParentSize()
                        .padding(top = 8.dp)
                        .background(Color.White, shape = RoundedCornerShape(4.dp))
                )
                OutlinedTextField(
                    value = search.value,
                    singleLine = true,
                    onValueChange = {
                        search.value = it
                        viewModel.onFriendSearchChange(it)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (friends.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    items(friends) { friend ->
                        FriendRow(friend, viewModel, scaffoldState.snackbarHostState)
                    }
                }
                BlackLine()
            } else {
                Spacer(Modifier.height(16.dp))
                Text("Start typing name of your friend...")
            }
        }
    }
}

@Composable
private fun FriendRow(
    friend: Friend,
    viewModel: AddFriendViewModel,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val isInvited = remember { mutableStateOf(false) }

    Column(Modifier.background(Color.White)) {
        BlackLine()
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(friend.fullName, fontWeight = FontWeight.Bold)
            Button(
                onClick = {
                    scope.launch {
                        val success = viewModel.onInviteFriendClicked(friend.authUserId!!)

                        if (success) {
                            isInvited.value = true
                            snackbarHostState.showSnackbar(message = "✔ Invite sent to ${friend.fullName}.")
                        }
                    }
                },
                enabled = !isInvited.value
            ) {
                Text(if (isInvited.value) "Invited" else "Invite")
            }
        }
    }
}