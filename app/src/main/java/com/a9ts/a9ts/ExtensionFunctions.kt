package com.a9ts.a9ts

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.a9ts.a9ts.Constants.Companion.CHANNEL_ID
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.text.Normalizer
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import kotlin.random.Random

fun Fragment.toast(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.toast(message: String) {
    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
}

fun View.snack(message: String, duration: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, message, duration).show()
}

// " Róbert  Joseph  Vereš" -> "robert joseph veres"
fun String.normalized() = Normalizer.normalize(this, Normalizer.Form.NFD)
    .replace("\\p{Mn}+".toRegex(), "")
    .replace("\\s+".toRegex(), " ")
    .toLowerCase(Locale.ROOT)
    .trim()

fun String.putLastWordFirst(): String? {
    if (!this.contains(' ')) return null

    val words = this.split(' ')


    var lastWord = ""
    var swapped = ""
    var delim = ""

    for (word in words) {
        swapped += "$delim$lastWord"
        lastWord = word
        delim = " "
    }

    return lastWord.plus(swapped)
}

fun toUTCTimestamp(localTimestampSeconds: Long): Long {
    val localDateTime = LocalDateTime.ofEpochSecond(localTimestampSeconds, 0, ZoneOffset.UTC)
    val dateTimeWithTimezone = ZonedDateTime.of(localDateTime, ZoneId.systemDefault())

    return dateTimeWithTimezone.toInstant().toEpochMilli() / 1000
}

suspend fun Task<*>.awaitWithStatus(): Boolean = try {
    this.await()
    true
} catch (e: FirebaseFirestoreException) {
    Timber.e(e)
    false
}

suspend fun <T> Task<T>.awaitOrNull(): T? = try {
    this.await()
} catch (e: FirebaseFirestoreException) {
    Timber.e(e)
    null
}


// toto zda sa pusta iba ked som v appke a nie ked som mimo appky
fun NotificationManager.sendNotification(message: RemoteMessage, appContext: Context) {
    val intent = Intent(appContext, MainActivity::class.java)

    //TODO mozno by rovnake notivikacie mali mat rovnake ID... notificationID = message.data["notificationId"]... Tym padom ak by prisla 2x ta ista zobrazi sa raz
    val notificationID = Random.nextInt()

    // If set, and the activity being launched is already running in the current task, then instead of launching a new instance of that activity,
    // all of the other activities on top of it will be closed and this Intent will be delivered to the (now on top) old activity as a new Intent.
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

    Timber.d("Notification received. Title: ${message.notification?.title}; Body: ${message.notification?.body};")

    //askmarcin not shure which intent to put here
    val pendingIntent = PendingIntent.getActivity(appContext, notificationID, intent, PendingIntent.FLAG_ONE_SHOT)



    val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
        .setContentTitle(message.notification?.title)
        .setContentText(message.notification?.body)
        .setSmallIcon(R.drawable.ic_calendar_icon)
        .setPriority(NotificationCompat.PRIORITY_HIGH) //Kvoli API < 26... Inak ide podla CHANNEL Importance
        .setAutoCancel(true) //notification automatically dismisses itself as it takes you to the app
        .setContentIntent(pendingIntent)
        .build()

    notify(notificationID, notification)
}

fun dateAndTimeFormatted(date: Date) : String {
    val dateText = DateFormat.format("E dd LLL", date).toString()
    val timeText = DateFormat.format("HH:mm", date).toString()
    return dateText.plus(", ").plus(timeText)
}






