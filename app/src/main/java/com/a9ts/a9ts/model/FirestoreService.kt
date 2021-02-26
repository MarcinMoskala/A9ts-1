package com.a9ts.a9ts.model

import com.a9ts.a9ts.*
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import timber.log.Timber

interface DatabaseService {
    suspend fun createUserProfile(user: User): Boolean

    fun hasProfileFilled(authUserId: String, onTrue: () -> Unit, onFalse: () -> Unit)
    suspend fun makeFriends(user1: User, user2: User): Boolean

    suspend fun getNonFriends(firstCharacters: String, currentUserId: String): List<Friend>?
    suspend fun getUser(authUserId: String): User?
    suspend fun getFriends(authUserId: String): List<Friend>
    suspend fun getNotificationsAndAppointments(authUserId: String): List<Any>?
    suspend fun sendAppointment(
        authUserId: String,
        friendUserId: String,
        dateTimeInSeconds: Long
    ): Boolean

    suspend fun sendFriendInvite(userId: String, friendUserId: String): Boolean
    suspend fun acceptFriendInvite(acceptingUserId: String, friendUserId: String): Boolean

}

class FirestoreService : DatabaseService {
    private val db = Firebase.firestore
    override suspend fun acceptFriendInvite(acceptingUserId: String, friendUserId: String): Boolean {
        return true
    }

    override suspend fun sendFriendInvite(userId: String, friendUserId: String): Boolean {
        try {
            val userDoc = db.collection(COLLECTION_USER_PROFILE).document(userId)
            val friendUserDoc = db.collection(COLLECTION_USER_PROFILE).document(friendUserId)
            val friendInvitationNotification = db.collection(COLLECTION_USER_PROFILE).document(friendUserId)
                .collection(COLLECTION_NOTIFICATION).document()

            db.runTransaction { transaction ->
                val snapshotUser = transaction.get(userDoc)
                val userFullName = snapshotUser.get("fullName").toString() //DONE askmarcin where should I define "fullName" as a constant?
                val userTelephone = snapshotUser.get("telephone").toString()

                val snapshotFriendUser = transaction.get(friendUserDoc)
                val friendUserFullName = snapshotFriendUser.get("fullName").toString()
                val friendUserTelephone = snapshotFriendUser.get("telephone").toString()

                // write to my /friends with I_INVITED state
                val usersFriendDoc = db.collection(COLLECTION_USER_PROFILE).document(userId).collection(COLLECTION_FRIEND).document(friendUserId)
                val usersFriend = Friend(friendUserId, friendUserFullName, Friend.STATUS_I_INVITED, friendUserTelephone)
                transaction.set(usersFriendDoc, usersFriend)

                // write to his /friends with I_AM_INVITED state
                val userFriendsFriendDoc = db.collection(COLLECTION_USER_PROFILE).document(friendUserId).collection(COLLECTION_FRIEND).document(userId)
                val userFriendsFriend = Friend(userId, userFullName, Friend.STATUS_I_AM_INVITED, userTelephone)
                transaction.set(userFriendsFriendDoc, userFriendsFriend)

                val invitation = Notification(
                    notificationType = Notification.TYPE_FRIEND_INVITATION,
                    fullName = userFullName,
                    authUserId = userId
                )
                transaction.set(friendInvitationNotification, invitation)

            }.await()

            //TODO send some kind of invitation notification

            return true
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun sendFriendInvite: {${e.message}")
            return false
        }

    }

    override suspend fun getNonFriends(firstCharacters: String, currentUserId: String): List<Friend>? {
        try {
            if (firstCharacters.isBlank()) {
                return listOf()
            }

            val firstCharactersNormalized = firstCharacters.normalized()

            val stringStart = firstCharactersNormalized.dropLast(1)

            // use .take (first X chars)
            val lastChar = firstCharactersNormalized.last()
            //pet pe t->u   pe + u = peu
            val stringEnd = stringStart.plus((lastChar.toInt() + 1).toChar())

            val fullTextUserEntries = db.collection(COLLECTION_USER_FULLTEXT)
                .whereGreaterThanOrEqualTo("fulltextName", firstCharactersNormalized)
                .whereLessThan("fulltextName", stringEnd)
                .get()
                .await()


            val users = mutableMapOf<String, Friend>()

            for (entry in fullTextUserEntries) {

                users[entry.get("authUserId").toString()] = Friend(
                    authUserId = entry.data["authUserId"] as String?,
                    fullName = entry.data["fullName"] as String?,
                )
            }

            // don't want to include myself
            users.remove(currentUserId)

            // don't want to include my existing friends (in any state of invitation / acceptance)
            val existingFriendEntries = db.collection(COLLECTION_USER_PROFILE).document(currentUserId).collection(COLLECTION_FRIEND).get().await()
            for (existingFriendEntry in existingFriendEntries) {
                users.remove(existingFriendEntry.id)
            }

            return users.values.toList()
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun sendAppointment: {${e.message}")
            return null
        }
    }

    // DONE askmarcin Not sure if this whole transaction is OK. This is how I understood it from docs...
    override suspend fun sendAppointment(
        authUserId: String,
        friendUserId: String,
        dateTimeInSeconds: Long
    ): Boolean {
        try {
            //get authUserFullname
            // get friendUserName
            // write Appointment

            val authUserDoc = db.collection(COLLECTION_USER_PROFILE).document(authUserId)
            val friendUserDoc = db.collection(COLLECTION_USER_PROFILE).document(friendUserId)
            val authUserAppointment =
                db.collection(COLLECTION_USER_PROFILE).document(authUserId).collection(
                    COLLECTION_APPOINTMENT
                ).document()
            val friendUserNotification =
                db.collection(COLLECTION_USER_PROFILE).document(friendUserId).collection(
                    COLLECTION_NOTIFICATION
                ).document()

            // DONE askmarcin How to read the documentation for runTransaction, still not sure what it does
            // how to read the documentation in general
            // ALSO: not sure what happens when it fails, it throws the exception?
            db.runTransaction { transaction ->
                val snapshotAuth = transaction.get(authUserDoc)
                val authUserFullName = snapshotAuth.get("fullName")

                val snapshotFriend = transaction.get(friendUserDoc)
                val friendUserFullName = snapshotFriend.get("fullName")

                val utcDateTimeInSeconds = toUTCTimestamp(dateTimeInSeconds)

                val appointment = hashMapOf(
                    "dateAndTime" to Timestamp(utcDateTimeInSeconds, 0),
                    "invitorName" to authUserFullName,
                    "inviteeName" to friendUserFullName,
                    "invitorUserId" to authUserId,
                    "inviteeUserId" to friendUserId
                )

                transaction.set(authUserAppointment, appointment)
                val notification = hashMapOf(
                    "dateAndTime" to Timestamp(utcDateTimeInSeconds, 0),
                    "notificationType" to Notification.TYPE_APP_INVITATION,
                    "fullName" to authUserFullName,
                    "authUserId" to authUserId,
                    "appointmentId" to authUserAppointment.id
                )

                transaction.set(friendUserNotification, notification)
            }.await()
            return true
        } catch (e: FirebaseFirestoreException) {
            //DONE askmarcin when to use Timber.e Timber.d etc...
            // and is this OK way to handle the exception?
            Timber.e("suspend fun sendAppointment: {${e.message}")
            return false
        }
    }


    override suspend fun getNotificationsAndAppointments(authUserId: String): List<Any> {
        return try {

            val notifications: List<Notification> =
                db.collection(COLLECTION_USER_PROFILE).document(authUserId)
                    .collection(COLLECTION_NOTIFICATION)
                    .get()
                    .await()
                    .toObjects()

            val appoinments: List<Appointment> =
                db.collection(COLLECTION_USER_PROFILE).document(authUserId)
                    .collection(COLLECTION_APPOINTMENT)
                    .get()
                    .await()
                    .toObjects()



            return notifications + appoinments

            // TODO not accepted invitations where authUserId == inviteeUserId should be on top, order by created time desc
            // rest should be under it order by DateTime asc
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun getAcceptedAppointments: {${e.message}")
            listOf()
        }
    }

    override suspend fun getFriends(authUserId: String): List<Friend> {
        return try {
            return db.collection(COLLECTION_USER_PROFILE).document(authUserId)
                .collection(COLLECTION_FRIEND)
                .whereGreaterThan("state", Friend.STATUS_I_AM_INVITED)
                .get()
                .await()
                .toObjects()
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun getFriends: {${e.message}")
            listOf()
        }
    }

    override suspend fun getUser(authUserId: String): User? = db.document("$COLLECTION_USER_PROFILE/$authUserId")
        .get()
        .awaitOrNull()
        ?.toObject<User>()

    override suspend fun makeFriends(user1: User, user2: User): Boolean {
        try {
            return if (user1.authUserId != null && user2.authUserId != null) {
                db.collection(COLLECTION_USER_PROFILE).document(user1.authUserId)
                    .collection(COLLECTION_FRIEND)
                    .document(user2.authUserId)
                    .set(user2)
                    .await() // can throw exception

                db.collection(COLLECTION_USER_PROFILE).document(user2.authUserId)
                    .collection(COLLECTION_FRIEND)
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

    override suspend fun createUserProfile(user: User): Boolean = db.runBatch { batch ->
        val userId = user.authUserId!!

        val profile = db.collection(COLLECTION_USER_PROFILE).document(userId)
        batch.set(profile, user)

        // Róbert Vereš -> robert veres
        val userNameNormalized = user.fullName!!.normalized()
        val fulltextUserEntry = db.collection(COLLECTION_USER_FULLTEXT).document()
        batch.set(fulltextUserEntry, hashMapOf("fulltextName" to userNameNormalized, "fullName" to user.fullName, "authUserId" to userId))

        // robert j. veres -> veres robert j.
        val userNameNormalizedSwapped = userNameNormalized.putLastWordFirst()

        if (userNameNormalizedSwapped != null) {
            val fulltextUserEntrySwapped = db.collection(COLLECTION_USER_FULLTEXT).document()
            batch.set(fulltextUserEntrySwapped, hashMapOf("fulltextName" to userNameNormalizedSwapped, "fullName" to user.fullName, "authUserId" to userId))
        }
    }.awaitWithStatus()

}



private const val COLLECTION_USER_PROFILE = "user_profile"
private const val COLLECTION_APPOINTMENT = "appointment"


private const val COLLECTION_NOTIFICATION = "notification"
private const val COLLECTION_USER_FULLTEXT = "user_fulltext"

private const val COLLECTION_FRIEND = "friend"




