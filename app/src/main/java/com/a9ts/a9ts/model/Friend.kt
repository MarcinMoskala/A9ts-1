package com.a9ts.a9ts.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Friend(
    @DocumentId val authUserId: String? = null,
    val fullName: String = "",
    var state: Int = -1,
    var telephone: String = "",
    @ServerTimestamp val created: Timestamp? = null
) {
    companion object {
        const val STATE_I_AM_INVITED = 0
        const val STATE_I_INVITED = 1
        const val STATE_ACCEPTED = 2
    }
}

