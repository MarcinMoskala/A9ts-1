package com.a9ts.a9ts.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp


data class Appointment(
    //should any of these be val instead of var?

    //this is not the final model... just something to try the RecyclerView on
    //i'll have to check how the NoSQL Firestore works, and what the best model should be

    var dateAndTime: Timestamp? = null,
    var invitorName: String? = null,
    var inviteeName: String? = null,
    var invitorUserId: String? = null,
    var inviteeUserId: String? = null,

    @ServerTimestamp var created: Timestamp? = null,
    var accepted: Timestamp? = null,
    var canceledByInvitor: Timestamp? = null,
    var canceledByInvitee: Timestamp? = null
)