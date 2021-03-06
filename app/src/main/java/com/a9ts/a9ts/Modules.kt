package com.a9ts.a9ts

import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.FirebaseAuthService
import com.a9ts.a9ts.model.FirestoreService
import org.koin.androidx.viewmodel.compat.ViewModelCompat.viewModel

import org.koin.dsl.module

val appModule = module {
    single <AuthService> {FirebaseAuthService()}
    single <DatabaseService> {FirestoreService()}
    single { MyFirebaseMessagingServiceController(get(), get()) }

//  askmarcin how does this work? https://insert-koin.io/docs/quickstart/android-viewmodel
//  viewModel { ActivityViewModel(get(), get()) }
}