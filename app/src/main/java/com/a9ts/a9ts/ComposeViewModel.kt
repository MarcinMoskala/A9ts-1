package com.a9ts.a9ts

import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a9ts.a9ts.model.*
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import kotlin.Exception

class ComposeViewModel : ViewModel(), KoinComponent {
    private val databaseService: DatabaseService by inject()
    private val authService: AuthService by inject()

    private val _deviceToken = MutableLiveData<String>()
    val deviceToken: LiveData<String> = _deviceToken

    private val _fullTelephoneNumber = MutableLiveData<String>()
    val fullTelephoneNumber: LiveData<String> = _fullTelephoneNumber

    // Auth step 1
    private val _countryCodeErrorMsg = MutableLiveData("")
    val countryCodeErrorMsg: LiveData<String> = _countryCodeErrorMsg

    private val _telephoneNumberErrorMsg = MutableLiveData("")
    val telephoneNumberErrorMsg: LiveData<String> = _telephoneNumberErrorMsg

    private val _telephoneFormSpinner = MutableLiveData(false)
    val telephoneFormSpinner: LiveData<Boolean> = _telephoneFormSpinner

    // Auth step 2
    private val _smsAndVerificationId = MutableLiveData(Pair("",""))
    val smsAndVerificationId: LiveData<Pair<String, String>> = _smsAndVerificationId

    private val _wrongSmsCode = MutableLiveData(false)
    val wrongSmsCode: LiveData<Boolean> = _wrongSmsCode

    // Main
    private var _aboutUser = MutableLiveData<String?>()
    val aboutUser: LiveData<String?>
        get() = _aboutUser

    private var _appointmentList = MutableLiveData<List<Appointment>>(listOf())
    val appointmentList: LiveData<List<Appointment>>
        get() = _appointmentList

    private var _notificationList = MutableLiveData<List<Notification>>(listOf())
    val notificationList: LiveData<List<Notification>>
        get() = _notificationList

    // AddAppointmentStepOne
    private val _addAppointmentStepOneFriends = MutableLiveData<List<Friend>>()
    val addAppointmentStepOneFriends : LiveData<List<Friend>>
        get() = _addAppointmentStepOneFriends

    // AddFriends
    private val _addFriendsList = MutableLiveData<List<Friend>>()
    val addFriendsList: LiveData<List<Friend>>
        get() = _addFriendsList

    suspend fun onInviteFriendClicked(userId : String) : Boolean { // askmarcin not sure if the snackbar call should be here...

        val userAndFriendUser : Pair<UserProfile, UserProfile>? = databaseService.sendFriendInvite(authService.authUserId, userId)

        return if (userAndFriendUser != null) {
            SystemPushNotification( //askmarcin - shouldn't this be in the viewholder?
                title = "Friend invitation from: ${(userAndFriendUser.first.fullName)}",
                body = "",
                token = (userAndFriendUser.second.deviceToken)
            ).also { sendSystemPushNotification(it) }
            true
        } else {
            false
        }
    }



    // General
    fun onSignInWithPhoneAuthCredential() {
        viewModelScope.launch {
            databaseService.updateDeviceToken(
                authUserId = authService.authUserId,
                deviceToken = FirebaseMessaging.getInstance().token.await()) {
                _deviceToken.value = it
            }
        }
    }

    // Auth step 1 --------------------------------------------------------------
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

    // AuthStep2 ------------------------------------------------------------------------------
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

    fun onAboutUser() {
        viewModelScope.launch {
            _aboutUser.value = databaseService.getUser(authService.authUserId)?.fullName
        }
    }

    fun onAboutUserShowed() {
        _aboutUser.value = null
    }

    fun onCancellationAccepted(appPartnerId: String, appointmentId: String, notificationId: String) {
        viewModelScope.launch {
            if (databaseService.acceptAppointmentCancellation(authService.authUserId, appPartnerId, appointmentId, notificationId)) {
                Timber.d("Cancellation accepted.")
            }
        }
    }

    // AddAppointmentStepOne

    fun onAddAppointmentStepOneInit() {
        viewModelScope.launch {
            _addAppointmentStepOneFriends.value =  databaseService.getFriends(authService.authUserId)
        }
    }

    // AddFriend

    fun onFriendSearchChange(s: String) {
        viewModelScope.launch {
            _addFriendsList.value = databaseService.getNonFriends(s, authService.authUserId)
        }
    }

}