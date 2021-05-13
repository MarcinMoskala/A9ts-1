package com.a9ts.a9ts.model.dataclass

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp


data class Appointment(
    @DocumentId val id : String? = null, // must be nullabe for Auto generated ID
    val dateAndTime: Timestamp = Timestamp.now(),
    val invitorName: String = "",
    val inviteeName: String = "",
    val invitorUserId: String = "",
    val inviteeUserId: String = "",
    val state: Int = -1,

    @ServerTimestamp val created: Timestamp? = null, // must be nullabe for Auto generated timestamp
    val accepted: Timestamp? = null, // TODO maybe NULL is not needed here
    val canceledByInvitor: Timestamp? = null,
    val canceledByInvitee: Timestamp? = null,
) {

    companion object  {

        const val STATE_I_AM_INVITED = 0
        const val STATE_I_INVITED = 1
        const val STATE_ACCEPTED = 2
        const val COLLECTION = "appointment"

    }
}