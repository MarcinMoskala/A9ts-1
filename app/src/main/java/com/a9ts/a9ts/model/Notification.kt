package com.a9ts.a9ts.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.time.LocalDate
import java.time.ZonedDateTime



data class Notification(
    @DocumentId val id: String? = null,
    var dateAndTime: Timestamp? = null,
    var notificationType: String? = null,
    var fullName: String? = null,
    var authUserId: String? = null,
    var appointmentId: String? = null,
    @ServerTimestamp var created: Timestamp? = null,
) {
    companion object {
        const val TYPE_APP_INVITATION = "appointment_invitation"
        const val TYPE_FRIEND_INVITATION = "friend_invitation"
    }
}

enum class NotificationType {
    APPOINTMENT_INVITATION,FRIEND_INVITATION
}