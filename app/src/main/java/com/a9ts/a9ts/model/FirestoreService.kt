package com.a9ts.a9ts.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import timber.log.Timber


interface DatabaseService {
    fun createUserProfile(
        user: User,
        success: () -> Unit,
        failure: (Exception) -> Unit
    )

    fun hasProfileFilled(authUserId: String, onTrue: () -> Unit, onFalse: () -> Unit)
    suspend fun makeFriends(user1: User, user2: User): Boolean

    suspend fun getUser(authUserId: String): User?
    suspend fun getFriends(authUserId: String): List<User>
    suspend fun getNotificationsAndAppointments(authUserId: String): List<Any>?
    suspend fun sendAppointment(authUserId: String, friendUserId: String, dateTimeInSeconds : Long) : Boolean

}

class FirestoreService : DatabaseService {
    private val db = Firebase.firestore



    // askmarcin Not sure if this whole transaction is OK. This is how I understood it from docs...
    override suspend fun sendAppointment(
        authUserId: String,
        friendUserId: String,
        dateTimeInSeconds: Long
    ) : Boolean {
        try {
            //get authUserFullname
            // get friendUserName
            // write Appointment

            val authUserDoc = db.collection(COLLECTION_USER_PROFILE).document(authUserId)
            val friendUserDoc = db.collection(COLLECTION_USER_PROFILE).document(friendUserId)
            val authUserAppointment = db.collection(COLLECTION_USER_PROFILE).document(authUserId).collection(
                COLLECTION_APPOINTMENT
            ).document()
            val friendUserNotification = db.collection(COLLECTION_USER_PROFILE).document(friendUserId).collection(
                COLLECTION_NOTIFICATION
            ).document()

            // askmarcin How to read the documentation for runTransaction, still not sure what it does
            // how to read the documentation in general
            // ALSO: not sure what happens when it fails, it throws the exception?
            db.runTransaction { transaction ->
                val snapshotAuth = transaction.get(authUserDoc)
                val authUserFullName = snapshotAuth.get("fullName")

                val snapshotFriend = transaction.get(friendUserDoc)
                val friendUserFullName = snapshotFriend.get("fullName")

                val appointment = hashMapOf(
                    "dateAndTime" to Timestamp(dateTimeInSeconds, 0),
                    "invitorName" to authUserFullName,
                    "inviteeName" to friendUserFullName,
                    "invitorUserId" to authUserId,
                    "inviteeUserId" to friendUserId
                )

                transaction.set(authUserAppointment, appointment)

                val notification = hashMapOf(
                    "dateAndTime" to Timestamp(dateTimeInSeconds, 0),
                    "notificationType" to NOTIFICATION_TYPE_INVITATION,
                    "fullName" to authUserFullName,
                    "authUserId" to authUserId,
                    "appointmentId" to authUserAppointment.id
                )

                transaction.set(friendUserNotification, notification)

                // Success askmarcin: not sure whay it should return null, took from an example
                null
            }.await()
            return true
        } catch  (e: FirebaseFirestoreException) {
            //askmarcin when to use Timber.e Timber.d etc...
            // and is this OK way to handle the exception?
            Timber.e("suspend fun sendAppointment: {${e.message}")
            return false
        }
    }


    override suspend fun getNotificationsAndAppointments(authUserId: String): List<Any> {
        return try {

            val notifications : List<Notification> = db.collection(COLLECTION_USER_PROFILE).document(authUserId)
                .collection(COLLECTION_NOTIFICATION)
                .get()
                .await()
                .toObjects()

            val appoinments : List<Appointment> = db.collection(COLLECTION_USER_PROFILE).document(authUserId)
                .collection(COLLECTION_APPOINTMENT)
                .get()
                .await()
                .toObjects()

            return notifications + appoinments

          // TODO
          // not accepted invitations where authUserId == inviteeUserId should be on top, order by created time desc
          // rest should be under it order by DateTime asc
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun getAcceptedAppointments: {${e.message}")
            listOf()
        }
    }

    override suspend fun getFriends(authUserId: String): List<User> {
        return try {
            return db.collection(COLLECTION_USER_PROFILE).document(authUserId).collection(COLLECTION_FRIEND)
                .get()
                .await()
                .toObjects()
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun getFriends: {${e.message}")
            listOf()
        }
    }

    override suspend fun getUser(authUserId: String): User? {
        return try {
            db.document("$COLLECTION_USER_PROFILE/$authUserId")
                .get()
                .await()
                .toObject<User>()
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun getUser: {${e.message}")
            null
        }
    }

    override suspend fun makeFriends(user1: User, user2: User): Boolean {
        try {
            return if (user1.authUserId != null && user2.authUserId != null) {
                db.collection(COLLECTION_USER_PROFILE).document(user1.authUserId).collection(COLLECTION_FRIEND)
                    .document(user2.authUserId)
                    .set(user2)
                    .await() // can throw exception

                db.collection(COLLECTION_USER_PROFILE).document(user2.authUserId).collection(COLLECTION_FRIEND)
                    .document(user1.authUserId)
                    .set(user1)
                    .await() // can throw exception
                true
            } else {
                false
            }
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun makeFriends: {${e.message}")
            return false
        }
    }

    override fun hasProfileFilled(authUserId: String, onTrue: () -> Unit, onFalse: () -> Unit) {
        db.collection(COLLECTION_USER_PROFILE).document(authUserId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc?.get("fullName") != null) {
                    onTrue()
                    return@addOnSuccessListener
                }

                onFalse()
            }
            .addOnFailureListener {
                Timber.d("databaseService.hasProfileFilled() failed")
            }
    }

    override fun createUserProfile(
        user: User,
        success: () -> Unit,
        failure: (Exception) -> Unit
    ) {
        db.collection(COLLECTION_USER_PROFILE).document(user.authUserId!!)
            .set(user)
            .addOnSuccessListener { success() }
            .addOnFailureListener(failure)
    }
}


private const val COLLECTION_USER_PROFILE = "user_profile"
private const val COLLECTION_APPOINTMENT = "appointment"
private const val COLLECTION_NOTIFICATION = "notification"
private const val COLLECTION_FRIEND = "friend"

const val NOTIFICATION_TYPE_INVITATION =  "invitation"

