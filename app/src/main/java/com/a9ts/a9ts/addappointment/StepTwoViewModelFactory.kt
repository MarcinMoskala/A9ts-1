package com.a9ts.a9ts.addappointment


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Simple ViewModel factory that provides the MarsProperty and context to the ViewModel.
 */
class StepTwoViewModelFactory(
    private val friendUserId: String,
    private val friendFullName: String) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StepTwoViewModel::class.java)) {
            return StepTwoViewModel(friendUserId, friendFullName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

