package com.a9ts.a9ts.components.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.dataclass.Friend
import com.a9ts.a9ts.model.dataclass.SystemPushNotification
import com.a9ts.a9ts.model.dataclass.UserProfile
import com.a9ts.a9ts.tools.sendSystemPushNotification
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddFriendViewModel: ViewModel(), KoinComponent {

    private val databaseService: DatabaseService by inject()
    private val authService: AuthService by inject()

    private val _addFriendsList = MutableLiveData<List<Friend>>()
    val addFriendsList: LiveData<List<Friend>>
        get() = _addFriendsList

    fun onFriendSearchChange(s: String) {
        viewModelScope.launch {
            _addFriendsList.value = databaseService.getNonFriends(s, authService.authUserId)
        }
    }

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


}