package com.a9ts.a9ts.detail


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Simple ViewModel factory that provides the MarsProperty and context to the ViewModel.
 */
class DetailViewModelFactory(private val appointmentId: String) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(appointmentId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

