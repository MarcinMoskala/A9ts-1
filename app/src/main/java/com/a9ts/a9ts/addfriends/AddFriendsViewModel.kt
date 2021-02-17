package com.a9ts.a9ts.addfriends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.a9ts.a9ts.MainActivity
import com.a9ts.a9ts.databinding.AddAppointmentStepOneFragmentBinding
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.User
import com.a9ts.a9ts.toast
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddFriendsViewModel : ViewModel(), KoinComponent {

    private val authService: AuthService by inject()

    private val databaseService: DatabaseService by inject()

    private val _newFriendsList = MutableLiveData<List<User>>()
    val newFriendsList : LiveData<List<User>>
        get() = _newFriendsList



    fun onTextChanged(s: CharSequence?) {
        viewModelScope.launch {
            _newFriendsList.value =  databaseService.getUsers(s.toString())
        }
    }


    init {
        viewModelScope.launch {
            _newFriendsList.value =  databaseService.getUsers()
        }
    }
}
