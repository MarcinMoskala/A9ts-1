package com.a9ts.a9ts.tools

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.core.app.NotificationCompat
import com.a9ts.a9ts.Activity
import com.a9ts.a9ts.R
import com.a9ts.a9ts.RetrofitInstance
import com.a9ts.a9ts.model.dataclass.Appointment
import com.a9ts.a9ts.model.dataclass.SystemPushNotification
import com.a9ts.a9ts.tools.Constants.Companion.CHANNEL_ID
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.text.Normalizer
import java.time.*
import java.util.*
import kotlin.random.Random


// " Róbert  Joseph  Vereš" -> "robert joseph veres"
fun String.normalized() = Normalizer.normalize(this, Normalizer.Form.NFD)
    .replace("\\p{Mn}+".toRegex(), "")
    .replace("\\s+".toRegex(), " ")
    .toLowerCase(Locale.ROOT)
    .trim()

fun Date.timeFormatted() = DateFormat.format("HH:mm", this).toString()
fun Date.dateFormatted() = DateFormat.format("E dd LLL", this).toString()

/*fun String.firstWord(): String {
    if (!this.contains(' ')) return this


    val i = input.indexOf(' ')
    val word = input.substring(0, i)
    val rest = input.substring(i)
}*/

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


fun toUTCTimestamp(localDate: LocalDate, localTime: LocalTime): Long {
    val localDateTime = LocalDateTime.of(
        localDate.year,
        localDate.month,
        localDate.dayOfMonth,
        localTime.hour,
        localTime.minute
    )

    val instant: Instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant()
    return instant.toEpochMilli() / 1000
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
    val intent = Intent(appContext, Activity::class.java)

    //TODO mozno by rovnake notivikacie mali mat rovnake ID... notificationID = message.data["notificationId"]... Tym padom ak by prisla 2x ta ista zobrazi sa raz
    val notificationID = Random.nextInt()

    // If set, and the activity being launched is already running in the current task, then instead of launching a new instance of that activity,
    // all of the other activities on top of it will be closed and this Intent will be delivered to the (now on top) old activity as a new Intent.
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

    Timber.d("Notification received. Title: ${message.notification?.title}; Body: ${message.notification?.body};")

    val pendingIntent = PendingIntent.getActivity(appContext, notificationID, intent, PendingIntent.FLAG_ONE_SHOT)

    val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
        .setContentTitle(message.notification?.title)
        .setContentText(message.notification?.body)
        .setSmallIcon(R.drawable.ic_launcher_obvio_foreground)
        .setPriority(NotificationCompat.PRIORITY_HIGH) //Kvoli API < 26... Inak ide podla CHANNEL Importance
        .setAutoCancel(true) //notification automatically dismisses itself as it takes you to the app
        .setContentIntent(pendingIntent)
        .build()

    notify(notificationID, notification)
}


fun dateAndTimeFormatted(date: Date): String {
    val dateText = DateFormat.format("E dd LLL", date).toString()
    val timeText = DateFormat.format("HH:mm", date).toString()
    return dateText.plus(", ").plus(timeText)
}


fun getMyIdAppointmentPartnerName(authUserID: String, invitorUserId: String, invitorFullName: String, inviteeFullName: String): String =
    if (authUserID == invitorUserId) inviteeFullName else invitorFullName

fun getMyIdAppointmentPartnerName(authUserID: String, appointment: Appointment): String =
    if (authUserID == appointment.invitorUserId) appointment.inviteeName else appointment.invitorName

suspend fun sendSystemPushNotification(systemNotification: SystemPushNotification) {
    try {
        val response = RetrofitInstance.api.postNotification(
            title = systemNotification.title,
            body = systemNotification.body,
            token = systemNotification.token
        )

        if (response.isSuccessful) {
            Timber.d("Response: $response")
        } else {
            val error = response.errorBody()
            Timber.e("Error: $error")
        }
    } catch (e: Exception) {
        Timber.e(e.toString())
    }
}







