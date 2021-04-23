package com.a9ts.a9ts

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.End
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a9ts.a9ts.ui.inputShape
import com.a9ts.a9ts.ui.theme.A9tsTheme

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            A9tsTheme {



                Scaffold(
                    content = {
                        TelephoneForm()
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun TelephoneForm() {
    val telephoneCodeState = remember { mutableStateOf("+421") }
    val telephoneNumberState = remember { mutableStateOf("") }
    val textState = remember { mutableStateOf("")}

    Column(Modifier.padding(16.dp)) {
        Row {

            OutlinedTextField(
                value = telephoneCodeState.value,
                singleLine = true,
                onValueChange = { telephoneCodeState.value = it },
                placeholder = { Text("+421") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.width(70.dp),
//                shape = inputShape,
                isError = true
            )

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = telephoneNumberState.value,
                singleLine = true,
                onValueChange = { telephoneNumberState.value = it },
                placeholder = { Text("9XX XXX XXX") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
//                shape = inputShape
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("GET SMS CODE")
        }



    }
}

