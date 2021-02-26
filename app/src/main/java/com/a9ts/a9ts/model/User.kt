package com.a9ts.a9ts.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class User (
    @DocumentId val authUserId: String? = null,
    var fullName: String? = null,
    val telephone: String? = null,
    @ServerTimestamp var created: Timestamp? = null
)