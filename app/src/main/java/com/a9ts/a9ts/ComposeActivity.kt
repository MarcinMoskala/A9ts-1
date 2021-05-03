package com.a9ts.a9ts

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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

    // TODO, when SMS is autofilled by the Phone, make it User friendly
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
            Timber.d("Auto-fill SMS code: ${credential.smsCode}")

            viewModel.onVerificationCompleted(credential.smsCode!!)
            //TODO fillSMSCode(smsCode = credential.smsCode.toString())

            signInWithPhoneAuthCredential(credential, wait = true)
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

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, wait : Boolean = false) {
        val waitMillis = if (wait) 500L else 0L
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
                            Timber.d("Navigating to main... User: ${authService.authUserId}")
                            navHostController.navigate("main")
                        }, waitMillis)
                    },
                    onFalse = { // navigate to Step 3
                        Handler(Looper.getMainLooper()).postDelayed({
                            // TODO: navigate to AuthStep 3
                            navHostController.navigate("authStepThree")
                            Timber.d("Navigating to AuthStep3...")
                        }, waitMillis)
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

        //askmarcin - not sure if those two viewModel call should be here... One of them needs Activity...
        // ---- AuthStep 1 ------------------------------------------------------------------------
        viewModel.fullTelephoneNumber.observe(this, { fullTelephoneNumber ->
            if (fullTelephoneNumber.isNotBlank()) {

                Timber.d("TelephonNumber is $fullTelephoneNumber")
                storedFullPhoneNumber = fullTelephoneNumber

                val options = PhoneAuthOptions.newBuilder(authService.getAuth())
                    .setPhoneNumber(fullTelephoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    // IMPORTANT: I need Activity here - (I think becasue of CAPTCHA in some cases)
                    .setActivity(this)
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        })

        // ---- AuthStep 2 ------------------------------------------------------------------------
        viewModel.smsAndVerificationId.observe(this, { pair ->
            if (pair.first != "" && pair.second != "") {
                val smsCode = pair.first
                val verificationId = pair.second
                val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)

                signInWithPhoneAuthCredential(credential)
            }
        })


        // ---- Toast -----------------------------------------------------------------------------
        viewModel.toastMessage.observe(this, { message ->
            if (message != "") {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        })


        setContent {
            navHostController = rememberNavController()
            val scaffoldState = rememberScaffoldState()

            A9tsTheme {
                NavHost(navHostController, startDestination = "authStepOne")
                {


                    // AuthStepOne
                    composable("authStepOne") {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text(text = "New Phone") }
                                )
                            },
                            content = {
                                AuthStepOne(viewModel)
                            }
                        )
                    }


                    // AuthStepTwo
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
                                AuthStepTwo(viewModel, verificationId!!)
                            }
                        )
                    }

                    // AuthStepThree
                    composable(
                        "authStepThree",
                    ) {

                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text(text = "Your profile") }
                                )
                            },
                            content = {
                                AuthStepThree(viewModel, navHostController)
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
                            topBar = {
                                TopAppBar(
                                    title = { Text(text = "My agenda") }
                                )
                            },
                            floatingActionButton = {
                                FloatingActionButton(
                                    onClick = { navHostController.navigate("addAppointmentStepOne") }
                                ) {
                                    Icon(Icons.Filled.Add, "")
                                }
                            })
                        {
                            Main(viewModel, navHostController, scaffoldState.snackbarHostState, authService.authUserId)
                        }
                    }


                    // AddAppointmentStepOne
                    composable(
                        "addAppointmentStepOne"
                    ) {
                        Scaffold(
                            backgroundColor = BgGrey,
                            topBar = {
                                TopAppBar(
                                    title = { Text(text = "New appointment with...") }
                                )
                            }
                        ) {
                            AddAppointmentStepOne(viewModel, navHostController, scaffoldState.snackbarHostState)
                        }
                    }


                    // AddAppointmentStepTwo
                    composable(
                        "addAppointmentStepTwo/{friendFullName}/{friendUserId}",
                        arguments = listOf(
                            navArgument("friendFullName") { type = NavType.StringType },
                            navArgument("friendUserId") { type = NavType.StringType })
                    ) {
                        val friendFullName = it.arguments?.getString("friendFullName")
                        val friendUserId = it.arguments?.getString("friendUserId")

                        Scaffold(
                            backgroundColor = BgGrey,
                            topBar = {
                                TopAppBar(
                                    title = { Text(text = "Appointment with $friendFullName") }
                                )
                            }
                        ) {
                            AddAppointmentStepTwo(viewModel, navHostController, friendUserId!!)
                        }
                    }


                    // AddFriend
                    composable(
                        "addFriend"
                    ) {
                        Scaffold(
                            backgroundColor = BgGrey,
                            scaffoldState = scaffoldState,
                            topBar = {
                                TopAppBar(
                                    title = { Text(text = "Invite friends") }
                                )
                            }
                        ) {
                            AddFriend(viewModel, scaffoldState.snackbarHostState, authService.authUserId)
                        }
                    }


                    // Appointment
                    composable(
                        "appointment/{appointmentId}",
                        arguments = listOf(
                            navArgument("appointmentId") { type = NavType.StringType })
                    ) {
                        val appointmentId = it.arguments?.getString("appointmentId")

                        Scaffold(
                            backgroundColor = BgGrey,
                            scaffoldState = scaffoldState,
                            topBar = {
                                TopAppBar(
                                    title = { Text(text = "Appointment") }
                                )
                            }
                        ) {
                            Appointment(viewModel, appointmentId!!)
                        }
                    }
                }
            }
        }
    }
}


