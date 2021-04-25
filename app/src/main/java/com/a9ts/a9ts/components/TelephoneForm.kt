package com.a9ts.a9ts.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.a9ts.a9ts.ComposeViewModel
import androidx.compose.runtime.getValue


@Composable
fun TelephoneForm(viewModel: ComposeViewModel) {
    val countryCode = remember { mutableStateOf("+1") }
    val telephoneNumber = remember { mutableStateOf("6505551234") }

    val countryCodeErrorMsg: String by viewModel.countryCodeErrorMsg.observeAsState("")
    val telephoneNumberErrorMsg: String by viewModel.telephoneNumberErrorMsg.observeAsState("")
    val telephoneFormSpinner: Boolean by viewModel.telephoneFormSpinner.observeAsState(false)

    Column(Modifier.padding(16.dp)) {
        Row {

            OutlinedTextField(
                value = countryCode.value,
//              label = { Text("Country Code") },
                singleLine = true,
                onValueChange = {
                    countryCode.value = it
                    viewModel.onCountryCodeKeyPressed()
                },
                placeholder = { Text("+421") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.width(70.dp),
                isError = countryCodeErrorMsg.isNotBlank(),
                enabled = !telephoneFormSpinner
            )

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = telephoneNumber.value,
                singleLine = true,
                onValueChange = {
                    telephoneNumber.value = it
                    viewModel.onTelephoneNumberKeyPressed()
                },
                placeholder = { Text("9XX XXX XXX") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                isError = telephoneNumberErrorMsg.isNotBlank(),
                enabled = !telephoneFormSpinner
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (countryCodeErrorMsg.isNotBlank() || telephoneNumberErrorMsg.isNotBlank()) {
            val errorMessage = "$countryCodeErrorMsg $telephoneNumberErrorMsg"
            Text(errorMessage, color = MaterialTheme.colors.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                viewModel.onSubmitTelephoneFormClicked(
                    telephoneNumber = telephoneNumber.value,
                    countryCode = countryCode.value
                )
            },
            modifier = Modifier.align(Alignment.End),
            enabled = !telephoneFormSpinner
        ) {
            Text("GET SMS CODE")
        }

        if (telephoneFormSpinner) {
            Spacer(Modifier.height(32.dp))
            CircularProgressIndicator(strokeWidth = 8.dp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}