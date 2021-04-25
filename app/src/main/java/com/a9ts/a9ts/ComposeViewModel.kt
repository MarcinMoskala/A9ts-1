package com.a9ts.a9ts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ComposeViewModel : ViewModel(), KoinComponent {
    private var storedFullPhoneNumber = ""

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

    // Auth step 1
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

    fun onCodeSent() {
        _telephoneFormSpinner.value = false
    }

    // AuthStep2
    fun onSmsCodeSubmitted(smsCode: String, verificationId: String) {
        _smsAndVerificationId.value = Pair(smsCode, verificationId)
    }

    fun onWrongSMSCode() {
        _wrongSmsCode.value = true
    }

    fun onSmsCodeKeyPressed() {
        _wrongSmsCode.value = false
    }


}