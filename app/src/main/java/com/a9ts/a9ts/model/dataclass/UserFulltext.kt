package com.a9ts.a9ts.model.dataclass

import com.google.firebase.firestore.DocumentId

data class UserFulltext(
    @DocumentId val id: String? = null,
    val authUserId: String = "",
    val fullName: String = "",
    val fulltextName: String = ""
) {
    companion object {
        const val COLLECTION = "user_fulltext"
    }
}
