package com.a9ts.a9ts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.a9ts.a9ts.ActivityViewModel

@Composable
fun MyTopBar(
    title: String,
    dropdown: Boolean = false,
    navHostController: NavHostController? = null,
    activityViewModel: ActivityViewModel? = null
) {
    TopAppBar(
        title = {
            Box(Modifier.fillMaxWidth()) {

                Text(
                    text = title,
                    modifier = Modifier.align(Alignment.BottomStart)
                ) // Title

                if (dropdown) {
                    var expanded by remember { mutableStateOf(false) }

                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 8.dp)
                    ) {
                        val fullName: String by activityViewModel!!.fullName.observeAsState("")
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier
                                .clickable { expanded = !expanded }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            offset = DpOffset(6.dp, 6.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                        ) {
                            Text(
                                text = "I'm $fullName",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.body1,
                            )
                            Divider()
                            DropdownMenuItem(onClick = { activityViewModel!!.onLogout(navHostController!!) }) {
                                Text("Logout", fontWeight = FontWeight.Bold)
                            }

                            DropdownMenuItem(onClick = { activityViewModel!!.onShowDeviceToken() }) {
                                Text("Show DeviceToken")
                            }
                        }
                    }
                }
            }
        },

        )
}