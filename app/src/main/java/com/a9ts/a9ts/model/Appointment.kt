package com.a9ts.a9ts.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp


data class Appointment(
    @DocumentId val id : String? = null, //null aby som ho mohol ignorovat pri zapise a vlozil ID sam za mna
    val dateAndTime: Timestamp = Timestamp.now(),
    val invitorName: String = "",
    val inviteeName: String = "",
    val invitorUserId: String = "",
    val inviteeUserId: String = "",
    val state: Int = -1,

    @ServerTimestamp val created: Timestamp? = null, //musim mat moznost dat null, lebo chcem poslat "null" aby vyplnil timestamp namiesto mna
    val accepted: Timestamp? = null,
    val canceledByInvitor: Timestamp? = null,
    val canceledByInvitee: Timestamp? = null,
) {
    companion object {
        const val STATE_I_AM_INVITED = 0
        const val STATE_I_INVITED = 1
        const val STATE_ACCEPTED = 2
        const val COLLECTION = "appointment"
    }
}