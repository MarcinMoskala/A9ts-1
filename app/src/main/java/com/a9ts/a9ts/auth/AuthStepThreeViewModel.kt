package com.a9ts.a9ts.auth

import androidx.lifecycle.*
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.UserProfile
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AuthStepThreeViewModel : ViewModel(), KoinComponent {

    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()


    private val _userProfileSubmitted = MutableLiveData<Boolean?>(null)
    val userProfileSubmitted: LiveData<Boolean?>
        get() = _userProfileSubmitted

    val fullName = MutableLiveData("")

    val valid = MediatorLiveData<Boolean>().apply {
        addSource(fullName) {
            value = it.isNotBlank()
        }
    }


    fun onSubmitClicked() {
        viewModelScope.launch {
            _userProfileSubmitted.value = databaseService.createUserProfile(
                UserProfile(authUserId = authService.authUserId,
                    fullName = fullName.value!!.trim(),
                    telephone = authService.getPhoneNumber(),
                    deviceToken = FirebaseMessaging.getInstance().token.await())
            )
        }
    }


    fun onSubmitClickedDone() {
        _userProfileSubmitted.value = null
    }

}