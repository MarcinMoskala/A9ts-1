package com.a9ts.a9ts.model.dataclass

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class UserProfile (
    @DocumentId val authUserId: String? = null,
    val fullName: String = "",
    val telephone: String = "",
    val deviceToken: String = "",
    @ServerTimestamp var created: Timestamp? = null

){
    companion object {
        const val COLLECTION = "user_profile"
    }
}