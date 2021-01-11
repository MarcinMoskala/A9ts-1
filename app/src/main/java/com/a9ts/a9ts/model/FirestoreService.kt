package com.a9ts.a9ts.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreService {
    val firestoreDB = FirebaseFirestore.getInstance()


    fun saveUser(
        userId: String,
        fullName: String,
        success: (Void) -> Unit,
        failure: (Exception) -> Unit
    ) {
        firestoreDB.collection(USERS).document(userId)
            .set(hashMapOf("name" to fullName))
            .addOnSuccessListener(success)
            .addOnFailureListener(failure)
    }





    private const val USERS = "users"

}