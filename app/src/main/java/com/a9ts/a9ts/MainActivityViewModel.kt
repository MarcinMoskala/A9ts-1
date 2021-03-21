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

class MainActivityViewModel : ViewModel(), KoinComponent {
    private val databaseService: DatabaseService by inject()
    private val authService: AuthService by inject()

    private val _deviceToken = MutableLiveData<String>()
    val deviceToken: LiveData<String>
        get() = _deviceToken

    // debug feature in Menu
    fun onUpdateDeviceToken() {
        viewModelScope.launch {
            databaseService.updateDeviceToken(
                authUserId = authService.authUserId,
                deviceToken = FirebaseMessaging.getInstance().token.await()) {
                    _deviceToken.value = it
            }
        }
    }
}