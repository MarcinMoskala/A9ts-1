package com.a9ts.a9ts

import android.app.Application
import timber.log.Timber
import timber.log.Timber.DebugTree


class ApplicationController : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
            Timber.d("testujem")
        }
    }
}