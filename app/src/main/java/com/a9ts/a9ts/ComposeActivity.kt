package com.a9ts.a9ts

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.ui.theme.A9tsTheme
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ComposeActivity : ComponentActivity() {
    private val viewModel: ComposeViewModel by viewModels()
    private val authService: AuthService by inject()
    val databaseService: DatabaseService by inject()
    private var storedFullPhoneNumber = ""
    lateinit var navController: NavHostController

    fun fillSMSCode(smsCode: String) {
        val editText = findViewById<EditText>(R.id.editTextVerificationCode)
        editText.setText(smsCode)
        editText.isEnabled = false

        val button = findViewById<Button>(R.id.button_send_code)
        button.isEnabled = false
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // automatic authentication without telephone number
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            Timber.d("onVerificationCompleted: ${credential.smsCode}")

            //TODO fillSMSCode(smsCode = credential.smsCode.toString())

            signInWithPhoneAuthCredential(credential)
        }

        //SMS cant be sent
        override fun onVerificationFailed(e: FirebaseException) {
            Timber.d("onVerificationFailed : ${e.message}")
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    // TODO: Vypisat Error ze cislo nie je ok
                    // binding.editTextPhoneNumber.error = getString(R.string.invalid_phone_number)
                }
                is FirebaseTooManyRequestsException -> {
                    Timber.d(getString(R.string.quota_exceeded))
                }
            }
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            Timber.d("SMS code sent to: $storedFullPhoneNumber")
            navController.navigate("authStepTwo/$verificationId/$storedFullPhoneNumber")
            // TODO navigate to step 2, send verificationId and storedFullPhoneNumber
//            findNavController().navigate(
//                AuthStepOneFragmentDirections.actionAuthStepOneFragmentToAuthStepTwoFragment(
//                    verificationId,
//                    storedFullPhoneNumber
//                )
//            )
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Timber.d("signInWithPhoneAuthCredential SmsCode: ${credential.smsCode}")

        authService.signInWithPhoneAuthCredential(
            this@ComposeActivity,
            credential,
            onSuccess = {
                viewModel.onUpdateDeviceToken()

                databaseService.hasProfileFilled(
                    authService.authUserId,
                    onTrue = { // navigate to mainFragment; add Logout to menu
                        Handler(Looper.getMainLooper()).postDelayed({

                            // TODO: navigate to main
                            Timber.d("Navigating to main...")
                            // mainAuthService.invalidateOptionsMenu()
                        }, 300)
                    },
                    onFalse = { // navigate to Step 3
                        Handler(Looper.getMainLooper()).postDelayed({
                            // TODO: navigate to AuthStep 3
                            Timber.d("Navigating to AuthStep3...")
                        }, 300)
                    }
                )
                Timber.d("Sign in success.")
            },
            onFailure = { exception ->
                Timber.d("Sign in failure: ${exception?.message}")
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.fullTelephoneNumber.observe(this, { fullTelephoneNumber ->
            if (fullTelephoneNumber.isNotBlank()) {

                Timber.d("TelephonNumber is $fullTelephoneNumber")
                storedFullPhoneNumber = fullTelephoneNumber

                val options = PhoneAuthOptions.newBuilder(authService.getAuth())
                    .setPhoneNumber(fullTelephoneNumber)   // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS)  // Timeout and unit
                    .setActivity(this)                  // Activity (for callback binding)
                    .setCallbacks(callbacks)           // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        })

        viewModel.smsAndVerificationId.observe(this, { pair ->
            if (pair.first != "" && pair.second != "") {
                val smsCode = pair.first
                val verificationId = pair.second
                val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)

                signInWithPhoneAuthCredential(credential)
            }
        })

        setContent {
            navController = rememberNavController()

            A9tsTheme {
                NavHost(navController, startDestination = "authStepOne")
                {
                    composable("authStepOne") {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text(text = "New Phone") }
                                )
                            },
                            content = {
                                TelephoneForm(viewModel)
                            }
                        )
                    }

                    composable(
                        "authStepTwo/{verificationId}/{fullPhoneNumber}",
                        arguments = listOf(
                            navArgument("verificationId") { type = NavType.StringType },
                            navArgument("fullPhoneNumber") { type = NavType.StringType })
                    ) {
                        val verificationId = it.arguments?.getString("verificationId")
                        val fullPhoneNumber = it.arguments?.getString("fullPhoneNumber")

                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text(text = fullPhoneNumber!!) }
                                )
                            },
                            content = {
                                SMSCodeForm(viewModel, verificationId!!)
                            }
                        )
                    }
                }


//                 vlastny Scaffold, ktory dostane parametre ako Nadpis, Ikony etc... v Navigation potom volat len tieto parametrizovane
//                 Scaffoldy

            }
        }
    }
}

@Composable
fun SMSCodeForm(viewModel: ComposeViewModel, verificationId: String) {

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val smsCode = remember { mutableStateOf("") }
        val maxChar = 6
        OutlinedTextField(
            value = smsCode.value,
            label = { Text("SMS Code") },
            textStyle = TextStyle(fontSize = 30.sp, textAlign = TextAlign.End, fontFamily = FontFamily.Monospace),
            singleLine = true,
            onValueChange = {
                if (it.length <= maxChar) smsCode.value = it},
            placeholder = { Text("000000", fontSize = 30.sp, textAlign = TextAlign.End, fontFamily = FontFamily.Monospace, modifier = Modifier.fillMaxWidth()) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .width(200.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
//            isError = countryCodeErrorMsg.isNotBlank(),
//            enabled = !loading
        )
        Spacer(Modifier.height(8.dp))

        Text("Enter the 6-digit verification code")

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                      viewModel.onSmsCodeSubmitted(smsCode.value, verificationId)
            },
//            enabled = !loading
        ) {
            Text("SEND VERIFICATION CODE")
        }

    }
}

@Composable
fun TelephoneForm(viewModel: ComposeViewModel) {
    val countryCode = remember { mutableStateOf("+1") }
    val telephoneNumber = remember { mutableStateOf("6505551234") }

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
                viewModel.onSubmitTelephoneFormClicked(
                    telephoneNumber = telephoneNumber.value,
                    countryCode = countryCode.value
                )
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

