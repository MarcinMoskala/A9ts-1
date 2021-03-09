package com.a9ts.a9ts

import android.app.NotificationManager
import android.content.SharedPreferences
import com.a9ts.a9ts.model.FirebaseAuthService
import com.a9ts.a9ts.model.FirestoreService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import timber.log.Timber

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object { //askmarcin - not sure why this is a good idea... is he using SharedPreferences as cache? It it normaly a slow operation?
        var sharedPref: SharedPreferences? = null
        private val databaseService = FirestoreService()
        private val authService = FirebaseAuthService()

        var token: String?
            get() {
                return sharedPref?.getString("token", "")
            }
            set(value) {
                databaseService.saveDeviceToken(authService.authUserId, value,
                onSuccess = {
                    sharedPref?.edit()?.putString("token", value)?.apply()
                    Timber.d("FCM token written to DB and sharedPrefs: $value")
                })
            }
    }


    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        token = newToken
    }

    override fun onMessageReceived(message: RemoteMessage) { //askmarcin how to fix "onMessageReceived(p0: RemoteMessage)" had to rename it myself
        super.onMessageReceived(message) //askmarcin sometime i see super. being call on the beginning of override fun, sometimes on the end, sometimes not at all...

        Timber.d("RemoteMessage: title=${message.notification?.title}; body=${message.notification?.body}")
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.sendNotification(message, this)

        //TODO pridat ACTIONS na ACCEPT... Tiez pozri design guides co a ako sa ma spravat po spravnu
        //https://material.io/design/platform-guidance/android-notifications.html#anatomy-of-a-notification
        //https://classroom.udacity.com/nanodegrees/nd940/parts/23bb2f5a-fe75-45e2-a108-212ab2b195c1/modules/716cd86f-fd2b-4c6d-92fc-d245b0bef01e/lessons/4024814a-4d91-4280-b6a7-8d2ee33a7b82/concepts/e67e3254-da48-419d-aff9-a3d8342db456
    }


}

