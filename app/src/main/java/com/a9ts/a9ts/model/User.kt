package com.a9ts.a9ts.model

import com.google.firebase.firestore.DocumentId

data class User (
    @DocumentId val authUserId: String? = null,
    var fullName: String? = null,
    val telephone: String? = null

    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
    //askmarcin - No Idea what it means

)