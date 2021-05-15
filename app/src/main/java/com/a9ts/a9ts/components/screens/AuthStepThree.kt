package com.a9ts.a9ts.components.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import androidx.navigation.compose.popUpTo
import com.a9ts.a9ts.ActivityViewModel
import com.a9ts.a9ts.components.MyTopBar
import kotlinx.coroutines.launch

@Composable
fun AuthStepThree(viewModel: ActivityViewModel, navHostController: NavHostController) {

    Scaffold(
        topBar = { MyTopBar("Your profile") },
    ) {
        Column(Modifier.padding(16.dp)) {
            val fullName = remember { mutableStateOf("") }
            val scope = rememberCoroutineScope()
            val minFullNameLength = 2

            OutlinedTextField(
                value = fullName.value,
                singleLine = true,
                onValueChange = {
                    fullName.value = it
                    viewModel.onTelephoneNumberKeyPressed()
                },
                placeholder = { Text("Your full name...") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
//          isError = telephoneNumberErrorMsg.isNotBlank(),
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Your friends will find you under this name.")
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (viewModel.onProfileFullNameSubmitted(fullName.value)) {
                            navHostController.navigate("agenda") {
                                popUpTo("authStepOne") { inclusive = true }
                            }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End),
//            enabled = !telephoneFormSpinner,
                enabled = fullName.value.trim().length > minFullNameLength

            ) {
                Text("DONE")
            }
        }
    }
}