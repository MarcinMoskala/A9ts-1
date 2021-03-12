package com.a9ts.a9ts

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import com.a9ts.a9ts.Constants.Companion.SHARED_PREFERENCES_NAME
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.FirebaseAuthService
import com.a9ts.a9ts.model.FirestoreService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject

import timber.log.Timber

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private val databaseService = FirestoreService() // askmarcin - whanted to have them here, but didn't know how to inject it
        private val authService = FirebaseAuthService()
//      private val authService: AuthService by inject() // Error: no value passed for clazz

        fun saveToken(context: Context, authUserId : String, token: String?)  {
            databaseService.saveDeviceToken(authService.authUserId, token,
                onSuccess = {
                    // MODE_PRIVATE - no other app can read our preferences
                    // apply - does it asynchronously
                    context.getSharedPreferences("$SHARED_PREFERENCES_NAME:$authUserId", MODE_PRIVATE)?.edit()?.putString("token", token)?.apply()
                    Timber.d("FCM token written to DB and sharedPrefs: $token")
                })
        }

        fun getToken(context: Context, authUserId : String): String? {
                return context.getSharedPreferences("$SHARED_PREFERENCES_NAME:$authUserId", MODE_PRIVATE)?.getString("token", null)
            }
    }


    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        saveToken(applicationContext, authService.authUserId, newToken)
    }

    override fun onMessageReceived(message: RemoteMessage) { // askmarcin how to fix "onMessageReceived(p0: RemoteMessage)" had to rename it myself
        super.onMessageReceived(message) // askmarcin sometime i see super. being call on the beginning of override fun, sometimes on the end, sometimes not at all...
        Timber.d("RemoteMessage: title=${message.notification?.title}; body=${message.notification?.body}")
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.sendNotification(message, this)

        //TODO pridat ACTIONS na ACCEPT... Tiez pozri design guides co a ako sa ma spravat po spravnu
        //https://material.io/design/platform-guidance/android-notifications.html#anatomy-of-a-notification
        //https://classroom.udacity.com/nanodegrees/nd940/parts/23bb2f5a-fe75-45e2-a108-212ab2b195c1/modules/716cd86f-fd2b-4c6d-92fc-d245b0bef01e/lessons/4024814a-4d91-4280-b6a7-8d2ee33a7b82/concepts/e67e3254-da48-419d-aff9-a3d8342db456
    }


}

