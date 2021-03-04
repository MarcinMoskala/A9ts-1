package com.a9ts.a9ts.auth

import androidx.lifecycle.*
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.UserProfile
import kotlinx.coroutines.launch
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


        viewModelScope.launch { // DONE askmarcin Should I use Dispatchers.IO? - not needed in Firebase, it's notblocking
            _userProfileSubmitted.value = databaseService.createUserProfile(
                UserProfile(authService.authUserId, fullName.value!!.trim(), authService.getPhoneNumber())
            )
        }
    }

    fun onSubmitClickedDone() {
        _userProfileSubmitted.value = null
    }

}