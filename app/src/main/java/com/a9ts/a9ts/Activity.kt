package com.a9ts.a9ts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.a9ts.a9ts.components.*
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.tools.Constants
import com.a9ts.a9ts.ui.theme.BgGrey
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

class Activity : ComponentActivity() {
    private val activityViewModel: ActivityViewModel by viewModels()

    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()

    private var storedFullPhoneNumber = ""
    lateinit var navHostController: NavHostController

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // automatic authentication without telephone number
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Timber.d("Auto-fill SMS code: ${credential.smsCode}")

            activityViewModel.onVerificationCompleted(credential.smsCode!!)
            signInWithPhoneAuthCredential(this@Activity, credential, wait = true)
        }

        //SMS cant be sent
        override fun onVerificationFailed(e: FirebaseException) {
            Timber.d("onVerificationFailed : ${e.message}")
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    activityViewModel.onVerificationFailed() // wrong telephone number
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
            activityViewModel.onCodeSent() // stop the spinner
        }
    }

    fun signInWithPhoneAuthCredential(
        activity: Activity,
        credential: PhoneAuthCredential,
        wait: Boolean = false
    ) {
        val waitMillis = if (wait) 500L else 0L
        Timber.d("signInWithPhoneAuthCredential SmsCode: ${credential.smsCode}")

        authService.signInWithPhoneAuthCredential(
            activity,
            credential,
            onSuccess = {
                activityViewModel.onSignInWithPhoneAuthCredential() // update device token

                Timber.d("Success: User = ${authService.authUserId}")

                databaseService.hasProfileFilled(
                    authService.authUserId,
                    onTrue = { // navigate Agenda
                        Handler(Looper.getMainLooper()).postDelayed({
                            Timber.d("Navigating to Agenda... User: ${authService.authUserId}")
                            navHostController.navigate("agenda")
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
                activityViewModel.onSignInWithPhoneAuthCredentialFailed(exception)
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createSystemNotificationChannel()

        activityViewModel.fullTelephoneNumber.observe(this, { fullTelephoneNumber ->
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
        activityViewModel.smsAndVerificationId.observe(this, { pair ->
            if (pair.first != "" && pair.second != "") {
                val smsCode = pair.first
                val verificationId = pair.second
                val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)

                signInWithPhoneAuthCredential(this, credential)
            }
        })


        // ---- Toast -----------------------------------------------------------------------------
        activityViewModel.toastMessage.observe(this, { message ->
            if (message != "") {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        })


        // --- NAVIGATION -------------------------------------------------------------------------
        // TODO: the navigation should be done with a sealed class, currently has to pass Strings as navigate paramaters
        // also quite a lot can be extracted, lot of code duplication right now
        setContent {
            navHostController = rememberNavController()
            val scaffoldState = rememberScaffoldState()

            A9tsTheme {
                NavHost(navHostController, startDestination = "agenda")
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
                                AuthStepOne(activityViewModel)
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
                                AuthStepTwo(activityViewModel, verificationId!!)
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
                                AuthStepThree(activityViewModel, navHostController)
                            }
                        )
                    }


                    // Agenda
                    composable(
                        "agenda"
                    ) {
                        Scaffold(
                            backgroundColor = BgGrey,
                            scaffoldState = scaffoldState,
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Box(Modifier.fillMaxWidth()) {
                                            var expanded by remember { mutableStateOf(false) }


                                            Text(
                                                text = "My agenda",
                                                modifier = Modifier.align(Alignment.BottomStart)
                                            ) // Title

                                            Box(
                                                Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(end = 8.dp)
                                            ) {
                                                val fullName: String by activityViewModel.fullName.observeAsState("")
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
                                                    DropdownMenuItem(onClick = { activityViewModel.onLogout(navHostController) }) {
                                                        Text("Logout", fontWeight = FontWeight.Bold)
                                                    }

                                                    DropdownMenuItem(onClick = { activityViewModel.onShowDeviceToken() }) {
                                                        Text("Show DeviceToken")
                                                    }
                                                }
                                            }
                                        }
                                    },

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
                            Agenda(navHostController, scaffoldState.snackbarHostState, authService.authUserId)
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
                            AddAppointmentStepOne(navHostController)
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
                            AddAppointmentStepTwo(activityViewModel, navHostController, friendUserId!!)
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
                            AddFriend(snackbarHostState = scaffoldState.snackbarHostState)
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
                            Appointment(appointmentId!!)
                        }
                    }
                }
            }
        }
    }

    private fun clearAllSystemNotifications() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun createSystemNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // leave here even when redundant, so I don't forget to put it back in case I'll deploy for <26
            val channel = NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                // TODO set it up properly with appropirate CHANNEL_NAME sensible default etc
                lightColor = android.graphics.Color.GREEN //rozsvieti LED na nasom telefon nejakou farbou
                enableLights(true)
                description = "New A9ts notification"
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}




