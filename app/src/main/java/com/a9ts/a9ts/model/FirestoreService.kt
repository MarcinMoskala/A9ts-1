package com.a9ts.a9ts.model

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import timber.log.Timber


interface DatabaseService {
    fun createUserProfile(
        user: User,
        success: () -> Unit,
        failure: (Exception) -> Unit
    )

    fun fillDatabaseWithData()
    fun hasProfileFilled(authUserId: String, onTrue: () -> Unit, onFalse: () -> Unit)
    suspend fun makeFriends(user1: User, user2: User) : Boolean

    suspend fun getUser(authUserId: String) : User?
}

class FirestoreService : DatabaseService {
    private val db = Firebase.firestore

    override suspend fun getUser(authUserId: String): User? {
        return try {
            db.document("$USER_PROFILE/$authUserId")
                .get()
                .await()
                .toObject<User>()
        } catch (e: java.lang.Exception) {
            null
        }
    }


    override suspend fun makeFriends(user1: User, user2: User) : Boolean  {
        try {
            if (user1.authUserId != null && user2.authUserId != null) {
                db.collection(USER_PROFILE).document(user1.authUserId).collection(FRIEND)
                    .document(user2.authUserId)
                    .set(user2)
                    .await()

                db.collection(USER_PROFILE).document(user2.authUserId).collection(FRIEND)
                    .document(user1.authUserId)
                    .set(user1)
                    .await()
                return true
            } else {
                return false
            }
        } catch (e: java.lang.Exception) {
            return false
        }
    }

    override fun hasProfileFilled(authUserId: String, onTrue: () -> Unit, onFalse: () -> Unit) {
        db.collection(USER_PROFILE).document(authUserId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc?.get("fullName") != null) {
                    onTrue()
                    return@addOnSuccessListener
                }

                onFalse()
            }
            .addOnFailureListener { Timber.d("databaseService.hasProfileFilled() failed") }
    }

    override fun fillDatabaseWithData() {

        val appointment = AppointmentRepository.appointmentList[0]
        db.collection(APPOINTMENT)
            .add(appointment)
            .addOnSuccessListener { doc -> Timber.d("Writen appointment with id: ${doc.id}") }
            .addOnFailureListener { e -> Timber.d("Error writing appointment: ${e.message}") }

    }


    override fun createUserProfile(
        user: User,
        success: () -> Unit,
        failure: (Exception) -> Unit
    ) {
        db.collection(USER_PROFILE).document(user.authUserId!!)
            .set(user)
            .addOnSuccessListener { success() }
            .addOnFailureListener(failure)
    }

    private val USER_PROFILE = "user_profile"
    private val APPOINTMENT = "appointment"
    private val FRIEND = "friend"

}