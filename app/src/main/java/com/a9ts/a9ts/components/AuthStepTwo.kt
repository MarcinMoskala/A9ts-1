package com.a9ts.a9ts.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a9ts.a9ts.ActivityViewModel
import androidx.compose.runtime.getValue

@Composable
fun AuthStepTwo(viewModel: ActivityViewModel, verificationId: String) {
    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val wrongSmsCode: Boolean by viewModel.wrongSmsCode.observeAsState(false)
        val smsCode = remember { mutableStateOf("") }
        val autoFilledSMS: String by viewModel.autoFilledSMS.observeAsState("")

        val autoFillSMSFilled = autoFilledSMS.isNotEmpty()

        if (autoFillSMSFilled) {
            smsCode.value = autoFilledSMS
        }

        val maxChar = 6

        OutlinedTextField(
            value = smsCode.value,
            label = { Text("Verification Code") },
            textStyle = TextStyle(fontSize = 30.sp, textAlign = TextAlign.End, fontFamily = FontFamily.Monospace),
            singleLine = true,
            onValueChange = {
                if (it.length <= maxChar) smsCode.value = it
                viewModel.onSmsCodeKeyPressed()
            },
            placeholder = { Text("000000", fontSize = 30.sp, textAlign = TextAlign.End, fontFamily = FontFamily.Monospace, modifier = Modifier.fillMaxWidth()) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .width(200.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            isError = wrongSmsCode,
            enabled = !autoFillSMSFilled
        )
        Spacer(Modifier.height(8.dp))

        if (wrongSmsCode) {
            Text("Wrong code. Please try again.", color = MaterialTheme.colors.error)
        } else {
            Text("Enter the 6-digit verification code")
        }
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.onSmsCodeSubmitted(smsCode.value, verificationId)
            },
            enabled = !autoFillSMSFilled && smsCode.value.length == maxChar
        ) {
            Text("SEND VERIFICATION CODE")
        }

    }
}