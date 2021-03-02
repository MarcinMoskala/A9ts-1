package com.a9ts.a9ts.model

import com.a9ts.a9ts.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import timber.log.Timber

interface DatabaseService {

    fun hasProfileFilled(authUserId: String, onTrue: () -> Unit, onFalse: () -> Unit)

    suspend fun createUserProfile(user: User): Boolean
    suspend fun makeFriends(user1: User, user2: User): Boolean

    suspend fun getNotificationsAndAppointments(authUserId: String): List<Any>?

    suspend fun sendAppointment(authUserId: String, friendUserId: String, dateTimeInSeconds: Long): Boolean

    suspend fun getUser(authUserId: String): User?
    suspend fun getFriends(authUserId: String): List<Friend>
    suspend fun getNonFriends(firstCharacters: String, currentUserId: String): List<Friend>?

    suspend fun sendFriendInvite(userId: String, friendUserId: String): Boolean
    suspend fun acceptFriendInvite(acceptingUserId: String, friendUserId: String, notificationId: String): Boolean
    suspend fun rejectFriendInvite(acceptingUserId: String, friendUserId: String, notificationId: String): Boolean
    suspend fun acceptAppointmentInvitation(authUserId: String, invitorUserId: String?, appointmentId: String?, notificationId: String?): Boolean
    suspend fun rejectAppointmentInvitation(authUserId: String, invitorUserId: String?, appointmentId: String?, notificationId: String?): Boolean


}

class FirestoreService : DatabaseService {
    private val db = Firebase.firestore

    override suspend fun sendAppointment(authUserId: String, friendUserId: String, dateTimeInSeconds: Long): Boolean {
        try {
            val authUserDoc = db.collection(COLLECTION_USER_PROFILE).document(authUserId)

            val friendUserDoc = db.collection(COLLECTION_USER_PROFILE).document(friendUserId)

            val authUserAppointment =
                db.collection(COLLECTION_USER_PROFILE).document(authUserId).collection(COLLECTION_APPOINTMENT).document()


            // oba Appointments nech maju same Id
            val friendUserAppointment =
                db.collection(COLLECTION_USER_PROFILE).document(friendUserId).collection(COLLECTION_APPOINTMENT).document(authUserAppointment.id)

            val friendUserNotification =
                db.collection(COLLECTION_USER_PROFILE).document(friendUserId).collection(COLLECTION_NOTIFICATION).document()

            db.runTransaction { transaction ->
                val authUser: User? = transaction.get(authUserDoc).toObject()
                val authUserFullName = authUser?.fullName

                val friendUser: User? = transaction.get(friendUserDoc).toObject()
                val friendUserFullName = friendUser?.fullName

                val utcDateTimeInSeconds = toUTCTimestamp(dateTimeInSeconds)


                val authUserAppointmentData = Appointment(
                    dateAndTime = Timestamp(utcDateTimeInSeconds, 0),
                    invitorName = authUserFullName.toString(),
                    inviteeName = friendUserFullName.toString(),
                    invitorUserId = authUserId,
                    inviteeUserId = friendUserId,
                    state = Appointment.STATE_I_INVITED
                )

                val friendUserAppointmentData = authUserAppointmentData.copy(
                    state = Appointment.STATE_I_AM_INVITED)

                val notification = Notification(
                    dateAndTime = Timestamp(utcDateTimeInSeconds, 0),
                    notificationType =Notification.TYPE_APP_INVITATION,
                    fullName = authUserFullName,
                    authUserId = authUserId,
                    appointmentId= authUserAppointment.id
                )


                transaction.set(authUserAppointment, authUserAppointmentData)


                transaction.set(friendUserAppointment, friendUserAppointmentData)
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

    override suspend fun acceptAppointmentInvitation(authUserId: String, invitorUserId: String?, appointmentId: String?, notificationId: String?): Boolean {
        val inviteeAppointment = db.collection(COLLECTION_USER_PROFILE).document(authUserId).collection(COLLECTION_APPOINTMENT).document(appointmentId.toString())
        val invitorAppointment = db.collection(COLLECTION_USER_PROFILE).document(invitorUserId!!).collection(COLLECTION_APPOINTMENT).document(appointmentId.toString())
        val notification     = db.collection(COLLECTION_USER_PROFILE).document(authUserId).collection(COLLECTION_NOTIFICATION).document(notificationId.toString())

        return try {
            db.runTransaction { transaction ->
//                askmarcin can I use reflection to get the "accepted" string?
//                transaction.update(inviteeAppoiment, Appointment::accepted.toString(), FieldValue.serverTimestamp())
//                transaction.update(invitorAppoiment, Appointment::accepted.toString(), FieldValue.serverTimestamp())

                  transaction.update(inviteeAppointment, "accepted", FieldValue.serverTimestamp(), "state", Appointment.STATE_ACCEPTED)
                  transaction.update(invitorAppointment, "accepted", FieldValue.serverTimestamp(),"state", Appointment.STATE_ACCEPTED)
                    transaction.delete(notification)
            }.await()
            true
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun acceptAppointmentInvitation: {${e.message}")
            false
        }

    }

    override suspend fun rejectAppointmentInvitation(authUserId: String, invitorUserId: String?, appointmentId: String?, notificationId: String?): Boolean {
        val inviteeAppointment = db.collection(COLLECTION_USER_PROFILE).document(authUserId).collection(COLLECTION_APPOINTMENT).document(appointmentId.toString())
        val invitorAppointment = db.collection(COLLECTION_USER_PROFILE).document(invitorUserId!!).collection(COLLECTION_APPOINTMENT).document(appointmentId.toString())
        val notification     = db.collection(COLLECTION_USER_PROFILE).document(authUserId).collection(COLLECTION_NOTIFICATION).document(notificationId.toString())

        return try {
            db.runTransaction { transaction ->
                transaction.delete(inviteeAppointment)
                transaction.delete(invitorAppointment)
                transaction.delete(notification)
            }.await()
            true
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun rejectAppointmentInvitation: {${e.message}")
            false
        }
    }

    override suspend fun acceptFriendInvite(acceptingUserId: String, friendUserId: String, notificationId: String): Boolean {
        val acceptingUserFriend = db.collection(COLLECTION_USER_PROFILE).document(acceptingUserId).collection(COLLECTION_FRIEND).document(friendUserId)
        val friendUserFriend = db.collection(COLLECTION_USER_PROFILE).document(friendUserId).collection(COLLECTION_FRIEND).document(acceptingUserId)
        val notification = db.collection(COLLECTION_USER_PROFILE).document(acceptingUserId).collection(COLLECTION_NOTIFICATION).document(notificationId)

        return try {
            db.runTransaction { transaction ->
                transaction.update(acceptingUserFriend, "state", Friend.STATE_ACCEPTED)
                transaction.update(friendUserFriend, "state", Friend.STATE_ACCEPTED)
                transaction.delete(notification)
            }.await()
            true
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun sendFriendInvite: {${e.message}")
            false
        }
    }

    override suspend fun rejectFriendInvite(acceptingUserId: String, friendUserId: String, notificationId: String): Boolean {
        val acceptingUserFriend = db.collection(COLLECTION_USER_PROFILE).document(acceptingUserId).collection(COLLECTION_FRIEND).document(friendUserId)
        val friendUserFriend = db.collection(COLLECTION_USER_PROFILE).document(friendUserId).collection(COLLECTION_FRIEND).document(acceptingUserId)
        val notification = db.collection(COLLECTION_USER_PROFILE).document(acceptingUserId).collection(COLLECTION_NOTIFICATION).document(notificationId)

        return try {
            db.runTransaction { transaction ->
                transaction.delete(acceptingUserFriend)
                transaction.delete(friendUserFriend)
                transaction.delete(notification)
            }.await()
            true
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun sendFriendInvite: {${e.message}")
            false
        }
    }

    override suspend fun sendFriendInvite(userId: String, friendUserId: String): Boolean {
        try {
            val userDoc = db.collection(COLLECTION_USER_PROFILE).document(userId)
            val friendUserDoc = db.collection(COLLECTION_USER_PROFILE).document(friendUserId)
            val friendInvitationNotification = db.collection(COLLECTION_USER_PROFILE).document(friendUserId)
                .collection(COLLECTION_NOTIFICATION).document()


            //Edge case:
            // ak uz mam zapis s tym friendom vo Friends tak return false (lebo tym padom sme friends, uz som ho pozval, alebo on pozval uz mna
            val iAmInvitedAlready = db.collection(COLLECTION_USER_PROFILE).document(userId).collection(COLLECTION_FRIEND).document(friendUserId).get().await()
            if (iAmInvitedAlready.exists()) return false

            db.runTransaction { transaction ->
                val snapshotUser = transaction.get(userDoc)
                val userFullName = snapshotUser.get("fullName").toString() //DONE askmarcin where should I define "fullName" as a constant?
                val userTelephone = snapshotUser.get("telephone").toString()

                val snapshotFriendUser = transaction.get(friendUserDoc)
                val friendUserFullName = snapshotFriendUser.get("fullName").toString()
                val friendUserTelephone = snapshotFriendUser.get("telephone").toString()

                // write to my /friends with I_INVITED state
                val usersFriendDoc = db.collection(COLLECTION_USER_PROFILE).document(userId).collection(COLLECTION_FRIEND).document(friendUserId)
                val usersFriend = Friend(friendUserId, friendUserFullName, Friend.STATE_I_INVITED, friendUserTelephone)
                transaction.set(usersFriendDoc, usersFriend)

                // write to his /friends with I_AM_INVITED state
                val userFriendsFriendDoc = db.collection(COLLECTION_USER_PROFILE).document(friendUserId).collection(COLLECTION_FRIEND).document(userId)
                val userFriendsFriend = Friend(userId, userFullName, Friend.STATE_I_AM_INVITED, userTelephone)
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
                    fullName = entry.data["fullName"] as String,
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
                    .whereGreaterThan("state", Appointment.STATE_I_AM_INVITED) //only if I_INVITED or ACCEPTED
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
                .whereGreaterThan("state", Friend.STATE_I_AM_INVITED)
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




