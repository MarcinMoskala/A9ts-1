package com.a9ts.a9ts

import android.app.NotificationManager
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import org.koin.android.ext.android.inject
// askmarcin in other ViewModels I have the "inject" like this
// import org.koin.core.component.inject and I need to extend KoinComponent...Why not here?

import timber.log.Timber

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val controller : MyFirebaseMessagingServiceController by inject()

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        controller.onNewToken(newToken)
    }

    // TODO zamysliet sa ako handlovat notifikacie ktore pridu kym som v appke... ci ich vobec treba... mozno hej, ak som mimo main fragment
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Timber.d("RemoteMessage received: title=${message.notification?.title}; body=${message.notification?.body}")

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.sendNotification(message, this)

        //TODO pridat ACTIONS na ACCEPT... Tiez pozri design guides co a ako sa ma spravat po spravnu
        //https://material.io/design/platform-guidance/android-notifications.html#anatomy-of-a-notification
        //https://classroom.udacity.com/nanodegrees/nd940/parts/23bb2f5a-fe75-45e2-a108-212ab2b195c1/modules/716cd86f-fd2b-4c6d-92fc-d245b0bef01e/lessons/4024814a-4d91-4280-b6a7-8d2ee33a7b82/concepts/e67e3254-da48-419d-aff9-a3d8342db456
    }
}

class MyFirebaseMessagingServiceController(
    private val databaseService: DatabaseService,
    private val authService: AuthService
) {
    fun onNewToken(newToken: String) {
        if (authService.isLogged) {
            databaseService.updateDeviceToken(
                authService.authUserId,
                newToken,
                onSuccess = {
                    // TODO tak ci tak by si to mal niekam poznacit a hned ako bude mat isLogged tak si to zapisat
                    Timber.d("FCM deviceToken updated in DB.: $newToken")
                })
        } else {
            Timber.e("FCM deviceToken... Tried to update... not Logged... Ignored.")
        }
    }
}