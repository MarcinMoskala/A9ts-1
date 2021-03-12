package com.a9ts.a9ts

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.a9ts.a9ts.Constants.Companion.SHARED_PREFERENCES_NAME
import com.a9ts.a9ts.model.AuthService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivityViewModel : ViewModel(), KoinComponent {
    private val authService: AuthService by inject()

    private val _authUserId = MutableLiveData<String>()
    val authUserId: LiveData<String>
        get() = _authUserId

    fun clearSharedPrefs(context: Context) {
        val sharedPrefName = "$SHARED_PREFERENCES_NAME:${authService.authUserId}"
        val pref: SharedPreferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.clear()
        editor.apply()
    }

    fun onCreateView() {
        _authUserId.value = authService.authUserId
    }
}