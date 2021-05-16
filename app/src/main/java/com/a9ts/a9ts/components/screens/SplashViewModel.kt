package com.a9ts.a9ts.components.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.dataclass.Appointment
import com.a9ts.a9ts.tools.Route
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SplashViewModel: ViewModel(), KoinComponent {
    private val databaseService: DatabaseService by inject()
    private val authService: AuthService by inject()

    private var _redirectTo = MutableLiveData("")
    val redirectTo: LiveData<String>
        get() = _redirectTo

    init {
        if (authService.isLogged) {
            _redirectTo.value = Route.AGENDA
        } else {
            _redirectTo.value = Route.AUTH_STEP_ONE
        }
    }
}