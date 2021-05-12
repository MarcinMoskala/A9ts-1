package com.a9ts.a9ts.model.dataclass

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp


data class Notification(
    @DocumentId val id: String? = null,
    var dateAndTime: Timestamp? = null,
    var notificationType: String? = null,
    var fullName: String = "",
    var authUserId: String = "",
    var appointmentId: String = NOT_SET, //TODO marcin
    @ServerTimestamp var created: Timestamp? = null,
) {
    companion object {
        const val NOT_SET = ""
        const val TYPE_APP_INVITATION = "appointment_invitation"
        const val TYPE_FRIEND_INVITATION = "friend_invitation"
        const val TYPE_CANCELLATION = "appointment_cancellation"

        const val COLLECTION = "notification"
    }
}
