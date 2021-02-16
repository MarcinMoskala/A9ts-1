package com.a9ts.a9ts.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp


data class Notification(
    var dateAndTime: Timestamp? = null,
    var notificationType: String? = null,
    var fullName: String? = null,
    var authUserId: String? = null,
    var appointmentId: String? = null,
    @ServerTimestamp var created: Timestamp? = null,
)