package com.a9ts.a9ts.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp


data class Appointment(
    var dateAndTime: Timestamp? = null,
    var invitorName: String? = null,
    var inviteeName: String? = null,
    var invitorUserId: String? = null,
    var inviteeUserId: String? = null,

    @ServerTimestamp var created: Timestamp? = null,
    var accepted: Timestamp? = null,
    var canceledByInvitor: Timestamp? = null,
    var canceledByInvitee: Timestamp? = null,
)