package com.a9ts.a9ts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigate
import com.a9ts.a9ts.model.*
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime

class ComposeViewModel : ViewModel(), KoinComponent {
    private val databaseService: DatabaseService by inject()
    private val authService: AuthService by inject()

    private val _deviceToken = MutableLiveData<String>()
    val deviceToken: LiveData<String> = _deviceToken

    private val _fullTelephoneNumber = MutableLiveData<String>()
    val fullTelephoneNumber: LiveData<String> = _fullTelephoneNumber

    // AuthStepOne
    private val _countryCodeErrorMsg = MutableLiveData("")
    val countryCodeErrorMsg: LiveData<String> = _countryCodeErrorMsg

    private val _telephoneNumberErrorMsg = MutableLiveData("")
    val telephoneNumberErrorMsg: LiveData<String> = _telephoneNumberErrorMsg

    private val _telephoneFormSpinner = MutableLiveData(false)
    val telephoneFormSpinner: LiveData<Boolean> = _telephoneFormSpinner

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



    // AddAppointmentStepOne
    private val _addAppointmentStepOneFriends = MutableLiveData<List<Friend>>()
    val addAppointmentStepOneFriends: LiveData<List<Friend>>
        get() = _addAppointmentStepOneFriends

    // AddFriends
    private val _addFriendsList = MutableLiveData<List<Friend>>()
    val addFriendsList: LiveData<List<Friend>>
        get() = _addFriendsList

    // AppointmentDetail
    private val _appointment = MutableLiveData<Appointment>()
    val appointment: LiveData<Appointment>
        get() = _appointment

    // UI
    private val _toastMessage = MutableLiveData<String>("")
    val toastMessage: LiveData<String>
        get() = _toastMessage


    suspend fun onInviteFriendClicked(userId: String): Boolean {

        val userAndFriendUser: Pair<UserProfile, UserProfile>? = databaseService.sendFriendInvite(authService.authUserId, userId)

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
                deviceToken = FirebaseMessaging.getInstance().token.await()
            ) {
                _deviceToken.value = it
            }
        }
    }

    // AuthSteoOne --------------------------------------------------------------
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


    // AddAppointmentStepOne --------------------------------------------

    fun onAddAppointmentStepOneInit() {
        viewModelScope.launch {
            _addAppointmentStepOneFriends.value = databaseService.getFriends(authService.authUserId)
        }
    }

    // AddAppointmentStepTwo --------------------------------------------

    suspend fun onAddAppointmentStepTwoSubmit(friendUserId: String, localDate: LocalDate, localTime: LocalTime): Boolean {

        val returnData = databaseService.sendAppointment(
            authService.authUserId,
            friendUserId,
            toUTCTimestamp(localDate, localTime)
        )

        return if (returnData != null) {
            val (notification, authUser, friendUser) = returnData

            val dateAndTime = dateAndTimeFormatted(notification.dateAndTime!!.toDate())


            //TODO make this faster and async
            Timber.d("Before notification")
            SystemPushNotification( //askmarcin this takes 2 seconds, how to make it faster, run in background...
                title = "Appointment invitation from: ${authUser.fullName}",
                body = dateAndTime,
                token = (friendUser).deviceToken
            ).also { sendSystemPushNotification(it) }
            Timber.d("After notification")


            _toastMessage.value = "✔ Invite sent to ${friendUser.fullName}"
            true
        } else {
            _toastMessage.value = "❌ Invite failed. Try again..."
            false
        }

    }


    // AddFriend --------------------------------------------------------

    fun onFriendSearchChange(s: String) {
        viewModelScope.launch {
            _addFriendsList.value = databaseService.getNonFriends(s, authService.authUserId)
        }
    }


    // AppointmentDetail ------------------------------------------------

    fun onAppointmentDetailInit(appointmentId: String) {
        databaseService.getAppointmentListener(appointmentId, authService.authUserId) { appointment ->
            _appointment.value = appointment
        }
    }

    fun cancelAppointment(appointment: Appointment) {
        viewModelScope.launch {
            databaseService.cancelAppointmentRequest(authService.authUserId, appointment)
            //TODO if toto je false tak nastav Toast<Livedata<String>> na nieco
        }
    }

    fun getAuthUserIdAppointmentPartnerName(appointment: Appointment): String {
        return getMyIdAppointmentPartnerName(authService.authUserId, appointment)
    }

    fun onVerificationCompleted(smsCode: String) {
        _autoFilledSMS.value = smsCode
    }


}