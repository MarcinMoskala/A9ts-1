package com.a9ts.a9ts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber
import kotlin.random.Random

private const val CHANNEL_ID   = "my_channel"
private const val CHANNEL_NAME = "channelName"


class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Timber.d("The token refreshed: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) { //askmarcin how to fix "onMessageReceived(p0: RemoteMessage)" had to rename it myself
        super.onMessageReceived(message) //askmarcin sometime i see super. being call on the beginning of override fun, sometimes on the end, sometimes not at all...

        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        @RequiresApi(Build.VERSION_CODES.O)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        // If set, and the activity being launched is already running in the current task, then instead of launching a new instance of that activity,
        // all of the other activities on top of it will be closed and this Intent will be delivered to the (now on top) old activity as a new Intent.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.ic_calendar_icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) //TODO not sure what it does exactly
            .setAutoCancel(true) //notification is automatically canceled when the user clicks it in the panel
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                lightColor = Color.GREEN //rozsvieti LED na nasom telefon nejakou farbou
                enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)

    }
}

/*private fun sendTestNotification() {

    val intent = Intent(this, MainActivity::class.java)
    val pendingIntent = TaskStackBuilder.create(this).run {
        addNextIntentWithParentStack(intent)
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Bobs notification title")
        .setContentText("This is the content text")
        .setSmallIcon(R.drawable.ic_calendar_icon)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .build()

    val notificationManager = NotificationManagerCompat.from(this)

    notificationManager.notify(NOTIFICATIONS_ID, notification)

}*/
