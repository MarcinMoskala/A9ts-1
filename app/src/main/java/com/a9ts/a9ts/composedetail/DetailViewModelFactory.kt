package com.a9ts.a9ts.composedetail


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.a9ts.a9ts.model.Appointment

/**
 * Simple ViewModel factory that provides the MarsProperty and context to the ViewModel.
 */
class DetailViewModelFactory(private val appointment: Appointment, private val app: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(appointment, app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

