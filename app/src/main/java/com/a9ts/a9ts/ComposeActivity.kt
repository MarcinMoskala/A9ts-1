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
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.a9ts.a9ts.components.*
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.ui.BgGrey
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
    private val databaseService: DatabaseService by inject()

    private var storedFullPhoneNumber = ""
    lateinit var navHostController: NavHostController

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
            Timber.d("SMS code: ${credential.smsCode}")

            //TODO fillSMSCode(smsCode = credential.smsCode.toString())

            signInWithPhoneAuthCredential(credential)
        }

        //SMS cant be sent
        override fun onVerificationFailed(e: FirebaseException) {
            Timber.d("onVerificationFailed : ${e.message}")
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    viewModel.onVerificationFailed() // wrong telephone number
                }

                is FirebaseTooManyRequestsException -> {
                    // fail silently
                    Timber.e(getString(R.string.quota_exceeded))
                }
            }
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            Timber.d("Verification code sent to: $storedFullPhoneNumber")

            navHostController.navigate("authStepTwo/$verificationId/$storedFullPhoneNumber")
            viewModel.onCodeSent() // stop the spinner
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Timber.d("signInWithPhoneAuthCredential SmsCode: ${credential.smsCode}")

        authService.signInWithPhoneAuthCredential(
            this@ComposeActivity,
            credential,
            onSuccess = {
                viewModel.onSignInWithPhoneAuthCredential() // update device token

                Timber.d("Success: User = ${authService.authUserId}")

                databaseService.hasProfileFilled(
                    authService.authUserId,
                    onTrue = { // navigate to mainFragment; add Logout to menu
                        Handler(Looper.getMainLooper()).postDelayed({

                            // TODO: navigate to main
                            Timber.d("Navigating to main... User: ${authService.authUserId}")
                            navHostController.navigate("main")
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
                viewModel.onSignInWithPhoneAuthCredentialFailed(exception)
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AuthStep 1
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

        // AuthStep 2
        viewModel.smsAndVerificationId.observe(this, { pair ->
            if (pair.first != "" && pair.second != "") {
                val smsCode = pair.first
                val verificationId = pair.second
                val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)

                signInWithPhoneAuthCredential(credential)
            }
        })

        setContent {
            navHostController = rememberNavController()
            val scaffoldState = rememberScaffoldState()

            A9tsTheme {
                NavHost(navHostController, startDestination = "add_friend")
                {

                    // Auth Step 1
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

                    // Auth Step 2
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
                                SmsCodeForm(viewModel, verificationId!!)
                            }
                        )
                    }

                    // Main
                    composable(
                        "main"
                    ) {
                        Scaffold(
                            backgroundColor = BgGrey,
                            scaffoldState = scaffoldState,
                            floatingActionButton = {
                                FloatingActionButton(
                                    onClick = { navHostController.navigate("add_appointment_step_1") }
                                ) {
                                    Icon(Icons.Filled.Add, "")
                                }
                            })
                        {
                            MainComponent(viewModel, navHostController, scaffoldState.snackbarHostState, authService.authUserId)
                        }
                    }

                    // AddAppointmentStepOne
                    composable(
                        "add_appointment_step_1"
                    ) {
                        Scaffold(
                            backgroundColor = BgGrey,
                        ) {
                            AddAppointmentStepOne(viewModel, navHostController, scaffoldState.snackbarHostState, authService.authUserId)
                        }
                    }

                    // AddFriend
                    composable(
                        "add_friend"
                    ) {
                        Scaffold(
                            backgroundColor = BgGrey,
                            scaffoldState = scaffoldState,
                        ) {
                            AddFriend(viewModel, navHostController, scaffoldState.snackbarHostState, authService.authUserId)
                        }
                    }
                }
            }
        }
    }
}

