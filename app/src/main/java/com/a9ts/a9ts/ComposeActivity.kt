package com.a9ts.a9ts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.a9ts.a9ts.ui.theme.A9tsTheme

class ComposeActivity : ComponentActivity() {
    private val viewModel: ComposeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            A9tsTheme {


                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = "New Phone") }
                        )},
                    content = {
                        TelephoneForm(viewModel)
                    }
                )
            }
        }
    }
}

@Composable
fun TelephoneForm(viewModel: ComposeViewModel) {
    val countryCode = remember { mutableStateOf("+421") }
    val telephoneNumber = remember { mutableStateOf("") }

    val countryCodeErrorMsg: String by viewModel.countryCodeErrorMsg.observeAsState("")
    val telephoneNumberErrorMsg: String by viewModel.telephoneNumberErrorMsg.observeAsState("")
    val loading: Boolean by viewModel.telephoneFormSpinner.observeAsState(false)

    Column(Modifier.padding(16.dp)) {
        Row {

            OutlinedTextField(
                value = countryCode.value,
                singleLine = true,
                onValueChange = { countryCode.value = it },
                placeholder = { Text("+421") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.width(70.dp),
                isError = countryCodeErrorMsg.isNotBlank(),
                enabled = !loading
            )

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = telephoneNumber.value,
                singleLine = true,
                onValueChange = { telephoneNumber.value = it },
                placeholder = { Text("9XX XXX XXX") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                isError = telephoneNumberErrorMsg.isNotBlank(),
                enabled = !loading
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val fullTelephoneNumber = viewModel.getFullTelephoneNumber(
                    telephoneNumber = telephoneNumber.value,
                    countryCode = countryCode.value
                )

//                if (fullTelephoneNumber) {
//
//
//                // no errors
//                    // - odosli na skontrolovanie do Firebase
//                    // - - zapni kolecko
//                    // - - nastav call back aby zrusil kolecko a siel na next step
//                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = !loading
        ) {
            Text("GET SMS CODE")
        }

        if (loading) {
                Spacer(Modifier.height(32.dp))
                CircularProgressIndicator(strokeWidth = 8.dp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

