package com.a9ts.a9ts.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.User
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class MainViewModel : ViewModel(), KoinComponent {
    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()

    private val _showUser = MutableLiveData<User>()
    val showUser: LiveData<User>
        get() = _showUser


    public fun isLogged(): Boolean {
        return authService.isLogged()
    }

    public fun createUserProfile() {
        databaseService.createUserProfile(
            User(authService.authUserId, "User Name", authService.getPhoneNumber()),
            success = { Timber.d("Save as ID: ${authService.getPhoneNumber()}") },
            failure = { exception -> Timber.d("Error while saving: ${exception.message}") }
        )
    }

    public fun fillDatabaseWithData() {
        databaseService.fillDatabaseWithData()
    }

    fun showUser() {
        viewModelScope.launch {
            _showUser.value = databaseService.getUser(authService.authUserId)
        }
    }

    fun showUserDone() {
        _showUser.value = null
    }

    fun befriendMarcinAndIgor() {
        viewModelScope.launch {
            val currentUser = databaseService.getUser(authService.authUserId)
            val marcinUser = databaseService.getUser("X8kEtA1z9BUMhMceRfeZJFpQqmJ2")
            val igorUser = databaseService.getUser("QTIFcGvQSJXLpr4pah8iOhFATyx1")


            currentUser?.let {
                marcinUser?.let {
                    databaseService.makeFriends(currentUser, marcinUser)
                }
                igorUser?.let {
                    databaseService.makeFriends(currentUser, igorUser)
                }

            }
        }
    }

}