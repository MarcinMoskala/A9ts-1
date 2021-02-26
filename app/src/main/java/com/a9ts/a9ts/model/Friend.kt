package com.a9ts.a9ts.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Friend(
    @DocumentId val authUserId: String? = null,
    val fullName: String? = null,
    var state: Int? = null,
    var telephone: String? = null,
    @ServerTimestamp val created: Timestamp? = null
) {
    companion object {
        const val STATUS_I_INVITED = 1
        const val STATUS_I_AM_INVITED = 0
        const val STATUS_ACCEPTED = 2
    }
}

