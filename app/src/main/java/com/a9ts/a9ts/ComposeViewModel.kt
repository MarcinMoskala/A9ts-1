package com.a9ts.a9ts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ComposeViewModel : ViewModel(), KoinComponent {
    private var storedFullPhoneNumber = ""

    private val databaseService: DatabaseService by inject()
    private val authService: AuthService by inject()


    // Auth step 1
    private val _countryCodeErrorMsg = MutableLiveData("")
    val countryCodeErrorMsg: LiveData<String> = _countryCodeErrorMsg

    private val _telephoneNumberErrorMsg = MutableLiveData("")
    val telephoneNumberErrorMsg: LiveData<String> = _telephoneNumberErrorMsg

    private val _telephoneFormSpinner = MutableLiveData(false)
    val telephoneFormSpinner: LiveData<Boolean> = _telephoneFormSpinner


    fun getFullTelephoneNumber(countryCode: String, telephoneNumber: String) : String {
        //TODO: more robust error checking
        _countryCodeErrorMsg.value = if (countryCode.trim().isBlank()) "Country can't be empty." else ""
        _telephoneNumberErrorMsg.value = if (telephoneNumber.trim().isBlank()) "Telephone can't be empty." else ""


        return if (countryCodeErrorMsg.value == "" && telephoneNumberErrorMsg.value == "") {

            _telephoneFormSpinner.value = true // ... Loading

            //TODO not fool proof
            var fullPhoneNumber = countryCode + telephoneNumber

            if (!fullPhoneNumber.startsWith("+")) {
                fullPhoneNumber = "+$fullPhoneNumber"
            }

            fullPhoneNumber
        } else {
            ""
        }

    }



}