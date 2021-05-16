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
import androidx.compose.material.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.a9ts.a9ts.components.screens.*
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.tools.Constants
import com.a9ts.a9ts.tools.Route
import com.a9ts.a9ts.ui.theme.A9tsTheme
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
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

            CoroutineScope(IO).launch {
                signInWithPhoneAuthCredential(credential, wait = true)
            }
        }

        //SMS cant be sent
        override fun onVerificationFailed(e: FirebaseException) {
            Timber.d("onVerificationFailed : ${e.message}")
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    activityViewModel.onVerificationFailed() // wroseng telephone number
                }

                is FirebaseTooManyRequestsException -> {
                    // fail silently
                    Timber.e(getString(R.string.quota_exceeded))
                }
            }
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            Timber.d("Verification code sent to: $storedFullPhoneNumber")
            activityViewModel.onCodeSent() // stop the spinner
            navHostController.navigate("${Route.AUTH_STEP_TWO}/$verificationId/$storedFullPhoneNumber")
        }
    }

    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, wait: Boolean = false) {
        val waitMillis = if (wait) 500L else 0L

        Timber.d("signInWithPhoneAuthCredential SmsCode: ${credential.smsCode}")

        val firebaseAuthResult = authService.signInWithPhoneAuthCredential(credential)

        if (firebaseAuthResult.user != null) {
                activityViewModel.onSignInWithPhoneAuthCredential() // update device token

                Timber.d("Success: User = ${authService.authUserId}")

                if (databaseService.hasProfileFilled(authService.authUserId)) {
                    Handler(Looper.getMainLooper()).postDelayed({ navHostController.navigate(Route.AGENDA) }, waitMillis)
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({ navHostController.navigate(Route.AUTH_STEP_THREE) }, waitMillis)
                }

                Timber.d("Sign in success.")
            } else {
                Timber.d("Sign in failure.")

                activityViewModel.onSignInWithPhoneAuthCredentialFailed()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createSystemNotificationChannel()

        // TODO
        // init mAuth = FirebaseAuth.getInstance();
        // init FirebaseAuth.AuthStateListener


        // ---- OBSERVERS -------------------------------------------------------------------------
        // ---- AuthStep 1 ------------------------------------------------------------------------

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

                CoroutineScope(IO).launch {
                    signInWithPhoneAuthCredential(credential)
                }
            }
        })


        // ---- Toast -----------------------------------------------------------------------------
        activityViewModel.toastMessage.observe(this, { message ->
            if (message != "") {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        })

        // TODO - create a splash screen wating for login, onLogin success -> agenda ELSE -> authStepOne

        // --- NAVIGATION -------------------------------------------------------------------------
        // TODO: the navigation should be done with a sealed class, currently has to pass Strings as navigate paramaters
        setContent {
            navHostController = rememberNavController()
            val scaffoldState = rememberScaffoldState() //TODO hot sure where to hold scaffoldState


            // TODO go to agenda - if not logged go to authStepOne
            A9tsTheme {
                NavHost(navHostController, startDestination = "splash")
                {
                    composable(Route.SPLASH) { Splash(navHostController) }

                    composable(Route.AUTH_STEP_ONE) { AuthStepOne(activityViewModel) }

                    composable(
                        route = "${Route.ADD_APPOINTMENT_STEP_TWO}/{verificationId}/{fullPhoneNumber}",
                        arguments = listOf(
                            navArgument("verificationId") { type = NavType.StringType },
                            navArgument("fullPhoneNumber") { type = NavType.StringType })
                    ) {
                        AuthStepTwo(
                            activityViewModel,
                            it.arguments?.getString("verificationId")!!,
                            it.arguments?.getString("fullPhoneNumber")!!
                        )
                    }

                    composable(Route.AUTH_STEP_THREE) { AuthStepThree(activityViewModel, navHostController) }

                    composable(Route.AGENDA) { Agenda(navHostController, scaffoldState = scaffoldState, authUserId = authService.authUserId) }

                    composable(Route.ADD_APPOINTMENT_STEP_ONE) { AddAppointmentStepOne(navHostController) }

                    composable(
                        route = "${Route.ADD_APPOINTMENT_STEP_TWO}/{friendFullName}/{friendUserId}",
                        arguments = listOf(
                            navArgument("friendFullName") { type = NavType.StringType },
                            navArgument("friendUserId") { type = NavType.StringType })
                    ) {
                        AddAppointmentStepTwo(
                            activityViewModel,
                            navHostController,
                            it.arguments?.getString("friendUserId")!!,
                            it.arguments?.getString("friendFullName")!!)
                    }

                    composable(Route.ADD_FRIEND) { AddFriend(scaffoldState = scaffoldState) }

                    composable(
                        route = "${Route.APPOINTMENT}/{appointmentId}",
                        arguments = listOf(
                            navArgument("appointmentId") { type = NavType.StringType })
                    ) {
                        Appointment(
                            it.arguments?.getString("appointmentId")!!,
                            scaffoldState = scaffoldState
                        )
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




