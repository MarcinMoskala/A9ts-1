package com.a9ts.a9ts

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.text.Normalizer
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

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

fun String.putLastWordFirst() : String? {
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

fun toUTCTimestamp(localTimestampSeconds : Long) : Long {
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

