package com.a9ts.a9ts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.a9ts.a9ts.ComposeViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.buttons
import com.vanpra.composematerialdialogs.datetime.datepicker
import com.vanpra.composematerialdialogs.datetime.timepicker.timepicker
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AddAppointmentStepTwo(
    viewModel: ComposeViewModel,
    navHostController: NavHostController,
    snackbarHostState: SnackbarHostState,
    friendUserId: String,
    friendFullname : String
) {


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val localDate = remember { mutableStateOf(LocalDate.now()) }
        val localTime = remember { mutableStateOf(LocalTime.now()) }
        val scope = rememberCoroutineScope()

        MyDateField(localDate)
        MyTimeField(localTime)


        Button(onClick = {
            scope.launch {
                val success = viewModel.onAddAppointmentStepTwoSubmit(friendUserId, localDate.value, localTime.value)

                if (success) {
                    snackbarHostState.showSnackbar(message = "✔ Invite sent to $friendFullname", null)
                } else {
                    snackbarHostState.showSnackbar(message = "❌ Invite failed. Try again.", null)
                }
            }
        }) {
            Text("Send invitation")
        }
    }
}


@Composable
fun MyTimeField(localTime: MutableState<LocalTime>) {
    val dialog = MaterialDialog()

    dialog.build {
        timepicker(initialTime = localTime.value) { time ->
            localTime.value = time
        }

        buttons {
            positiveButton("Ok")
            negativeButton("Cancel")
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        val myTimeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        val localDateFormatted = localTime.value.format(myTimeFormat)

        ReadonlyTextField(
            value = TextFieldValue(localDateFormatted),
            onValueChange = { },
            onClick = { dialog.show() },
//          label = { Text(text = "Time") }
        )
    }
}

@Composable
fun MyDateField(localDate: MutableState<LocalDate>) {
    val dialog = MaterialDialog()

    dialog.build {
        datepicker(initialDate = localDate.value) { date ->
            localDate.value = date
        }

        buttons {
            positiveButton("Ok")
            negativeButton("Cancel")
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        val myDateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        val localDateFormatted = localDate.value.format(myDateFormat)

        ReadonlyTextField(
            value = TextFieldValue(localDateFormatted),
            onValueChange = { },
            onClick = { dialog.show() },
//          label = { Text(text = "Date") }
        )
    }
}

@Composable
fun ReadonlyTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    label: @Composable () -> Unit = {}
) {
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            label = label
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0f)
                .clickable(onClick = onClick),
        )
    }
}
