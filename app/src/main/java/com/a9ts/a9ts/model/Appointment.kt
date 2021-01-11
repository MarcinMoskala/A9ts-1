package com.a9ts.a9ts.model

import java.time.LocalDateTime


data class Appointment(
    //should any of these be val instead of var?

    //this is not the final model... just something to try the RecyclerView on
    //i'll have to check how the NoSQL Firestore works, and what the best model should be

    var id: Long,
    var dateAndTime: LocalDateTime,
    var description: String,
    var invitorName: String,
    var inviteeName: String,

    var created: LocalDateTime,
    var accepted: LocalDateTime?,
    var canceledByInvitor: LocalDateTime?,
    var canceledByInvitee: LocalDateTime?
)