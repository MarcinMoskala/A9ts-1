package com.a9ts.a9ts

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.dataclass.Appointment
import com.a9ts.a9ts.model.dataclass.Notification
import com.a9ts.a9ts.model.dataclass.UserProfile
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class ActivityViewModel : ViewModel(), KoinComponent {
    private val databaseService: DatabaseService by inject()
    private val authService: AuthService by inject()

    private val _deviceToken = MutableLiveData<String>()
    val deviceToken: LiveData<String> = _deviceToken

    private val _fullTelephoneNumber = MutableLiveData<String>()
    val fullTelephoneNumber: LiveData<String> = _fullTelephoneNumber

    private val _countryCodeErrorMsg = MutableLiveData("")
    val countryCodeErrorMsg: LiveData<String> = _countryCodeErrorMsg

    private val _telephoneNumberErrorMsg = MutableLiveData("")
    val telephoneNumberErrorMsg: LiveData<String> = _telephoneNumberErrorMsg

    private val _telephoneFormSpinner = MutableLiveData(false)
    val telephoneFormSpinner: LiveData<Boolean> = _telephoneFormSpinner

    fun onVerificationFailed() {
        _telephoneFormSpinner.value = false
        _telephoneNumberErrorMsg.value = "Invalid telephone number."
    }

    fun onCodeSent() {
        _telephoneFormSpinner.value = false
    }

    fun onCountryCodeKeyPressed() {
        if (countryCodeErrorMsg.value.toString().isNotBlank()) _countryCodeErrorMsg.value = ""
    }

    fun onTelephoneNumberKeyPressed() {
        if (telephoneNumberErrorMsg.value.toString().isNotBlank()) _telephoneNumberErrorMsg.value = ""
    }
    fun onSubmitTelephoneFormClicked(countryCode: String, telephoneNumber: String) {
        //TODO: more robust error checking
        _countryCodeErrorMsg.value = if (countryCode.trim().isBlank()) "Country code can't be empty." else ""
        _telephoneNumberErrorMsg.value = if (telephoneNumber.trim().isBlank()) "Telephone can't be empty." else ""

        if (countryCodeErrorMsg.value == "" && telephoneNumberErrorMsg.value == "") {
            _telephoneFormSpinner.value = true // ... Loading
            //TODO not fool proof
            var fullPhoneNumber = countryCode + telephoneNumber

            if (!fullPhoneNumber.startsWith("+")) {
                fullPhoneNumber = "+$fullPhoneNumber"
            }

            _fullTelephoneNumber.value = fullPhoneNumber
        }
    }


    // AuthStepTwo
    private val _smsAndVerificationId = MutableLiveData(Pair("", ""))
    val smsAndVerificationId: LiveData<Pair<String, String>> = _smsAndVerificationId

    private val _wrongSmsCode = MutableLiveData(false)
    val wrongSmsCode: LiveData<Boolean> = _wrongSmsCode

    // AuthStepThree
    private val _autoFilledSMS = MutableLiveData("")
    val autoFilledSMS: LiveData<String> = _autoFilledSMS


    // Main
    private var _appointmentList = MutableLiveData<List<Appointment>>(listOf())
    val appointmentList: LiveData<List<Appointment>>
        get() = _appointmentList

    private var _notificationList = MutableLiveData<List<Notification>>(listOf())
    val notificationList: LiveData<List<Notification>>
        get() = _notificationList

    private var _fullName = MutableLiveData("")
    val fullName: LiveData<String>
        get() = _fullName


    // UI
    private val _toastMessage = MutableLiveData<String>("")
    val toastMessage: LiveData<String>
        get() = _toastMessage


    // General
    fun onSignInWithPhoneAuthCredential() {
        viewModelScope.launch {
            databaseService.updateDeviceToken(
                authUserId = authService.authUserId,
                deviceToken = FirebaseMessaging.getInstance().token.await()
            ) {
                _deviceToken.value = it
            }
        }
    }

    fun onShowDeviceToken() {
        viewModelScope.launch {
            val deviceToken = FirebaseMessaging.getInstance().token.await()
            _toastMessage.value = deviceToken
        }
    }

    // AuthStepTwo ------------------------------------------------------------------------------
    fun onSmsCodeSubmitted(smsCode: String, verificationId: String) {
        _smsAndVerificationId.value = Pair(smsCode, verificationId)
    }

    fun onSignInWithPhoneAuthCredentialFailed(exception: Exception?) {
        if (exception is FirebaseAuthInvalidCredentialsException) {
            _wrongSmsCode.value = true
        } else {
            Timber.e(exception)
        }

    }

    fun onSmsCodeKeyPressed() {
        if (wrongSmsCode.value!!) _wrongSmsCode.value = false
    }


    // AuthStepThree ----------------------------------------------------

    suspend fun onProfileFullNameSubmitted(fullName: String): Boolean {
        return databaseService.createUserProfile(
            UserProfile(
                authUserId = authService.authUserId,
                fullName = fullName.trim(),
                telephone = authService.getPhoneNumber(),
                deviceToken = FirebaseMessaging.getInstance().token.await()
            )
        )
    }


    //Main ------------------------------------------------------------------------------------

    // askmarcin where and how to initiali this?
    // TODO next
    fun onMainInit() {
        Timber.d("fired...")
        databaseService.getAppointmentsListener(authService.authUserId) { appointmentList ->
            _appointmentList.value = appointmentList
        }

        databaseService.getNotificationsListener(authService.authUserId) { notificationList ->
            _notificationList.value = notificationList
        }

        viewModelScope.launch {
            _fullName.value = databaseService.getUser(authService.authUserId)?.fullName
        }
    }

    fun onAppointmentNotificationAccepted(invitorUserId: String, appointmentId: String, notificationId: String) {
        viewModelScope.launch {
            if (databaseService.acceptAppointmentInvitation(authService.authUserId, invitorUserId, appointmentId, notificationId)) {
                Timber.d("✔ Appointment accepted.")
            }
        }
    }

    fun onAppointmentNotificationRejected(invitorUserId: String, appointmentId: String, notificationId: String) {
        viewModelScope.launch {
            if (databaseService.rejectAppointmentInvitation(authService.authUserId, invitorUserId, appointmentId, notificationId)) {
// TODO         _snackMessage.value = "❌ Appointment rejected"
                Timber.d("❌ Appointment rejected.")
            }
        }
    }

    fun onFriendNotificationAccepted(authUserId: String, notificationId: String) {

        viewModelScope.launch {
            if (databaseService.acceptFriendInvite(authService.authUserId, authUserId, notificationId)) {
                Timber.d("✔ Friendship accepted.")
            }
        }
    }

    fun onFriendNotificationRejected(authUserId: String, notificationId: String) {
        viewModelScope.launch {
            if (databaseService.rejectFriendInvite(authService.authUserId, authUserId, notificationId)) {
                Timber.d("❌ Friendship request rejected.")
            }
        }
    }

    fun onCancellationAccepted(appPartnerId: String, appointmentId: String, notificationId: String) {
        viewModelScope.launch {
            if (databaseService.acceptAppointmentCancellation(authService.authUserId, appPartnerId, appointmentId, notificationId)) {
                Timber.d("Cancellation accepted.")
            }
        }
    }

    fun onLogout(navHostController: NavHostController) {
        authService.signOut()
        navHostController.navigate("authStepOne")
        _toastMessage.value = "You were logged out."
    }

    fun onVerificationCompleted(smsCode: String) {
        _autoFilledSMS.value = smsCode
    }

    fun setToast(message: String) {
        _toastMessage.value = message
    }


}