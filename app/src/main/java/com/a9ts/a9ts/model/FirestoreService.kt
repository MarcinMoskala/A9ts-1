package com.a9ts.a9ts.model

import com.a9ts.a9ts.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber

interface DatabaseService {

    fun hasProfileFilled(authUserId: String, onTrue: () -> Unit, onFalse: () -> Unit)

    suspend fun createUserProfile(user: UserProfile): Boolean
    suspend fun makeFriends(user1: UserProfile, user2: UserProfile): Boolean

    suspend fun getNotificationsAndAppointments(authUserId: String): List<Any>? //TODO make it into a sealed class

    suspend fun sendAppointment(authUserId: String, friendUserId: String, dateTimeInSeconds: Long): Boolean

    suspend fun getUser(authUserId: String): UserProfile?
    suspend fun getFriends(authUserId: String): List<Friend>?
    suspend fun getNonFriends(firstCharacters: String, currentUserId: String): List<Friend>?

    suspend fun sendFriendInvite(userId: String, friendUserId: String): Boolean
    suspend fun acceptFriendInvite(acceptingUserId: String, friendUserId: String, notificationId: String): Boolean
    suspend fun rejectFriendInvite(acceptingUserId: String, friendUserId: String, notificationId: String): Boolean
    suspend fun acceptAppointmentInvitation(authUserId: String, invitorUserId: String?, appointmentId: String?, notificationId: String?): Boolean
    suspend fun rejectAppointmentInvitation(authUserId: String, invitorUserId: String?, appointmentId: String?, notificationId: String?): Boolean

    suspend fun cancelAppointmentRequest(invitorIsCanceling: Boolean, invitorId: String, inviteeId: String, appointmentId: String): Boolean

    fun getNotificationsListener(authUserId: String, onSuccess: (List<Notification>) -> Unit)
    fun getAppointmentsListener(authUserId: String, onSuccess: (List<Appointment>) -> Unit)
    fun updateDeviceToken(authUserId: String, deviceToken: String?, onSuccess: (deviceToken: String) -> Unit)
    fun getAppointmentListener(appointmentId: String, authUserId: String, onSuccess: (appointment : Appointment?) -> Unit)
}

class FirestoreService : DatabaseService {
    private val db = Firebase.firestore

    override fun getAppointmentListener(appointmentId: String, authUserId: String, onSuccess: (appointment : Appointment?) -> Unit)
    {
        db.collection(UserProfile.COLLECTION).document(authUserId).collection(Appointment.COLLECTION).document(appointmentId)
            .addSnapshotListener { appointmentSnapshot, e ->
                if (e != null) {
                    Timber.w("getAppointmentListener failed: $e")
                    return@addSnapshotListener
                }

                var appointment : Appointment? = null

                if (appointmentSnapshot != null) {
                    appointment = appointmentSnapshot.toObject()
                }

                onSuccess(appointment)
            }
    }

    override suspend fun cancelAppointmentRequest(invitorIsCanceling: Boolean, invitorId: String, inviteeId: String, appointmentId: String): Boolean =
        db.runBatch { batch ->
            val appointmentInvitor = db.collection(UserProfile.COLLECTION).document(invitorId).collection(Appointment.COLLECTION).document(appointmentId)
            val appointmentInvitee = db.collection(UserProfile.COLLECTION).document(inviteeId).collection(Appointment.COLLECTION).document(appointmentId)

            if (invitorIsCanceling) {
                batch.update(appointmentInvitor, Appointment::canceledByInvitor.name, FieldValue.serverTimestamp())
                batch.update(appointmentInvitee, Appointment::canceledByInvitor.name, FieldValue.serverTimestamp())
            } else {
                batch.update(appointmentInvitor, Appointment::canceledByInvitee.name, FieldValue.serverTimestamp())
                batch.update(appointmentInvitee, Appointment::canceledByInvitee.name, FieldValue.serverTimestamp())
            }
        }.awaitWithStatus()

    override fun updateDeviceToken(authUserId: String, deviceToken: String?, onSuccess: (deviceToken: String) -> Unit) {
        db.collection(UserProfile.COLLECTION).document(authUserId)
            .set(mapOf(UserProfile::deviceToken.name to deviceToken!!), SetOptions.merge())
            .addOnSuccessListener {
                onSuccess(deviceToken)
            }
            .addOnFailureListener { e ->
                Timber.e("databaseService.saveToken() failed: $e")
            }
    }

    override fun getNotificationsListener(authUserId: String, onSuccess: (List<Notification>) -> Unit) {
        db.collection(UserProfile.COLLECTION).document(authUserId).collection(Notification.COLLECTION)
            .orderBy("created", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, e ->
                Timber.d("getNotificationListener")
                if (e != null) {
                    Timber.w("getNotificationsListener failed: $e")
                    return@addSnapshotListener
                }

                val notificationList = ArrayList<Notification>()
                if (querySnapshot != null) {
                    for (element in querySnapshot) {
                        notificationList.add(element.toObject())
                    }
                }

                onSuccess(notificationList)
            }
    }

    override fun getAppointmentsListener(authUserId: String, onSuccess: (List<Appointment>) -> Unit) {
        db.collection(UserProfile.COLLECTION).document(authUserId).collection(Appointment.COLLECTION)
            .whereGreaterThan("state", Appointment.STATE_I_AM_INVITED)
            .addSnapshotListener { querySnapshot, e ->
                Timber.d("getAppointmentListener")

                if (e != null) {
                    Timber.w("getAppointmentListener failed: $e")
                    return@addSnapshotListener
                }

                // kvoli naslednemu sortingu davam do ArrayList
                val appointmentList = ArrayList<Appointment>()
                if (querySnapshot != null) {
                    for (element in querySnapshot) {
                        appointmentList.add(element.toObject())
                    }
                }

                onSuccess(appointmentList.sortedWith(compareBy { it.dateAndTime }))
            }
    }

    override suspend fun sendAppointment(authUserId: String, friendUserId: String, dateTimeInSeconds: Long): Boolean =
        try {
            val authUserDoc = db.collection(UserProfile.COLLECTION).document(authUserId)
            val friendUserDoc = db.collection(UserProfile.COLLECTION).document(friendUserId)
            val authUserAppointment =
                db.collection(UserProfile.COLLECTION).document(authUserId).collection(Appointment.COLLECTION).document()

            // oba Appointments nech maju same Id
            val friendUserAppointment =
                db.collection(UserProfile.COLLECTION).document(friendUserId).collection(Appointment.COLLECTION).document(authUserAppointment.id)

            val friendUserNotification =
                db.collection(UserProfile.COLLECTION).document(friendUserId).collection(Notification.COLLECTION).document()

            val (notification, authUser, friendUser) = db.runTransaction { transaction ->
                val authUser: UserProfile? = transaction.get(authUserDoc).toObject()
                val authUserFullName = authUser?.fullName!!

                val friendUser: UserProfile? = transaction.get(friendUserDoc).toObject()
                val friendUserFullName = friendUser?.fullName!!

                val utcDateTimeInSeconds = toUTCTimestamp(dateTimeInSeconds)

                val authUserAppointmentData = Appointment(
                    dateAndTime = Timestamp(utcDateTimeInSeconds, 0),
                    invitorName = authUserFullName,
                    inviteeName = friendUserFullName,
                    invitorUserId = authUserId,
                    inviteeUserId = friendUserId,
                    state = Appointment.STATE_I_INVITED
                )

                val friendUserAppointmentData = authUserAppointmentData.copy(
                    state = Appointment.STATE_I_AM_INVITED
                )

                val notification = Notification(
                    dateAndTime = Timestamp(utcDateTimeInSeconds, 0),
                    notificationType = Notification.TYPE_APP_INVITATION,
                    fullName = authUserFullName,
                    authUserId = authUserId,
                    appointmentId = authUserAppointment.id
                )

                transaction.set(authUserAppointment, authUserAppointmentData)
                transaction.set(friendUserAppointment, friendUserAppointmentData)
                transaction.set(friendUserNotification, notification)
                Triple(notification, authUser, friendUser)
            }.await()

            // send systemPushNotification
            val dateAndTime = dateAndTimeFormatted(notification.dateAndTime!!.toDate())
            SystemPushNotification(
                title = "Appointment invitation from: ${authUser.fullName}",
                body = dateAndTime,
                token = (friendUser).deviceToken
            ).also { sendSystemPushNotification(it) }

            true
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun sendAppointment: {${e.message}")
            false
        }


    override suspend fun acceptAppointmentInvitation(authUserId: String, invitorUserId: String?, appointmentId: String?, notificationId: String?): Boolean {
        val inviteeAppointment = db.collection(UserProfile.COLLECTION).document(authUserId).collection(Appointment.COLLECTION).document(appointmentId.toString())
        val invitorAppointment = db.collection(UserProfile.COLLECTION).document(invitorUserId!!).collection(Appointment.COLLECTION).document(appointmentId.toString())
        val notification = db.collection(UserProfile.COLLECTION).document(authUserId).collection(Notification.COLLECTION).document(notificationId.toString())

        return try {
            db.runTransaction { transaction ->

                transaction.update(inviteeAppointment, Appointment::accepted.name, FieldValue.serverTimestamp(), Appointment::state.name, Appointment.STATE_ACCEPTED)
                transaction.update(invitorAppointment, Appointment::accepted.name, FieldValue.serverTimestamp(), Appointment::state.name, Appointment.STATE_ACCEPTED)
                transaction.delete(notification)
            }.await()
            true
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun acceptAppointmentInvitation: {${e.message}")
            false
        }

    }

    override suspend fun rejectAppointmentInvitation(authUserId: String, invitorUserId: String?, appointmentId: String?, notificationId: String?): Boolean {
        val inviteeAppointment = db.collection(UserProfile.COLLECTION).document(authUserId).collection(Appointment.COLLECTION).document(appointmentId.toString())
        val invitorAppointment = db.collection(UserProfile.COLLECTION).document(invitorUserId!!).collection(Appointment.COLLECTION).document(appointmentId.toString())
        val notification = db.collection(UserProfile.COLLECTION).document(authUserId).collection(Notification.COLLECTION).document(notificationId.toString())

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
        val acceptingUserFriend = db.collection(UserProfile.COLLECTION).document(acceptingUserId).collection(Friend.COLLECTION).document(friendUserId)
        val friendUserFriend = db.collection(UserProfile.COLLECTION).document(friendUserId).collection(Friend.COLLECTION).document(acceptingUserId)
        val notification = db.collection(UserProfile.COLLECTION).document(acceptingUserId).collection(Notification.COLLECTION).document(notificationId)

        return try {
            db.runTransaction { transaction ->
                transaction.update(acceptingUserFriend, Friend::state.name, Friend.STATE_ACCEPTED)
                transaction.update(friendUserFriend, Friend::state.name, Friend.STATE_ACCEPTED)
                transaction.delete(notification)
            }.await()
            true
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun acceptFriendInvite: {${e.message}")
            false
        }
    }

    override suspend fun rejectFriendInvite(acceptingUserId: String, friendUserId: String, notificationId: String): Boolean {
        val acceptingUserFriend = db.collection(UserProfile.COLLECTION).document(acceptingUserId).collection(Friend.COLLECTION).document(friendUserId)
        val friendUserFriend = db.collection(UserProfile.COLLECTION).document(friendUserId).collection(Friend.COLLECTION).document(acceptingUserId)
        val notification = db.collection(UserProfile.COLLECTION).document(acceptingUserId).collection(Notification.COLLECTION).document(notificationId)

        return try {
            db.runTransaction { transaction ->
                transaction.delete(acceptingUserFriend)
                transaction.delete(friendUserFriend)
                transaction.delete(notification)
            }.await()
            true
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun rejectFriendInvite: {${e.message}")
            false
        }
    }

    override suspend fun sendFriendInvite(userId: String, friendUserId: String): Boolean {
        val userProfileDoc = db.collection(UserProfile.COLLECTION).document(userId)
        val friendUserDocProfile = db.collection(UserProfile.COLLECTION).document(friendUserId)
        val friendInvitationNotification = db.collection(UserProfile.COLLECTION).document(friendUserId)
            .collection(Notification.COLLECTION).document()


        //Edge case:
        // ak uz mam zapis s tym friendom vo Friends tak return false (lebo tym padom sme friends, uz som ho pozval, alebo on pozval uz mna
        val iAmInvitedAlready = db.collection(UserProfile.COLLECTION).document(userId).collection(Friend.COLLECTION).document(friendUserId).get().await()


        if (iAmInvitedAlready.exists()) return false

        return try {
            val (user, friendUser) = db.runTransaction { transaction ->

                val friendUser: UserProfile = transaction.get(friendUserDocProfile).toObject()!!
                val user: UserProfile = transaction.get(userProfileDoc).toObject()!!

                // write to my /friends with I_INVITED state
                val usersFriendDoc = db.collection(UserProfile.COLLECTION).document(userId).collection(Friend.COLLECTION).document(friendUserId)
                val usersFriend = Friend(friendUserId, fullName = friendUser.fullName, state = Friend.STATE_I_INVITED, telephone = friendUser.telephone)
                transaction.set(usersFriendDoc, usersFriend)

                // write to his /friends with I_AM_INVITED state
                val userFriendsFriendDoc = db.collection(UserProfile.COLLECTION).document(friendUserId).collection(Friend.COLLECTION).document(userId)
                val userFriendsFriend = Friend(userId, fullName = user.fullName, state = Friend.STATE_I_AM_INVITED, telephone = user.telephone)
                transaction.set(userFriendsFriendDoc, userFriendsFriend)

                val invitation = Notification(
                    notificationType = Notification.TYPE_FRIEND_INVITATION,
                    fullName = user.fullName,
                    authUserId = userId
                )

                transaction.set(friendInvitationNotification, invitation)
                Pair(user, friendUser)

            }.await()

            SystemPushNotification(
                title = "Friend invitation from: ${(user.fullName)}",
                body = "",
                token = (friendUser.deviceToken)
            ).also { sendSystemPushNotification(it) }

            //TODO moze sa stat ze uspesne zapise ale neuspesne posle systemNotification...Nie je uplny pruser, ale stoji za zamyslenie

            true
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun sendFriendInvite: {${e.message}")
            false
        }
    }


    /* override suspend fun sendFriendInvite(userId: String, friendUserId: String): Boolean {
         val userProfileDoc = db.collection(UserProfile.COLLECTION).document(userId)
         val friendUserDocProfile = db.collection(UserProfile.COLLECTION).document(friendUserId)
         val friendInvitationNotification = db.collection(UserProfile.COLLECTION).document(friendUserId)
             .collection(Notification.COLLECTION).document()


         //Edge case:
         // ak uz mam zapis s tym friendom vo Friends tak return false (lebo tym padom sme friends, uz som ho pozval, alebo on pozval uz mna
         val iAmInvitedAlready = db.collection(UserProfile.COLLECTION).document(userId).collection(Friend.COLLECTION).document(friendUserId).get().await()

         if (iAmInvitedAlready.exists()) return false

         val friendUser : UserProfile = friendUserDocProfile.get().await().toObject()!!

         val user : UserProfile = userProfileDoc.get().await().toObject()!!

         // write to my /friends with I_INVITED state
         val usersFriendDoc = db.collection(UserProfile.COLLECTION).document(userId).collection(Friend.COLLECTION).document(friendUserId)
         val usersFriend = Friend(friendUserId, fullName = friendUser.fullName, state = Friend.STATE_I_INVITED, telephone = friendUser.telephone)
         usersFriendDoc.set(usersFriend).await()

         // write to his /friends with I_AM_INVITED state
         val userFriendsFriendDoc = db.collection(UserProfile.COLLECTION).document(friendUserId).collection(Friend.COLLECTION).document(userId)
         val userFriendsFriend = Friend(userId, fullName = user.fullName, state = Friend.STATE_I_AM_INVITED, telephone = user.telephone)
         userFriendsFriendDoc.set(userFriendsFriend).await()

         val invitation = Notification(
             notificationType = Notification.TYPE_FRIEND_INVITATION,
             fullName = user.fullName,
             authUserId = userId
         )

         friendInvitationNotification.set(invitation).await()
         Timber.d("ok 7")

         return false
     }*/

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

            val fullTextUserEntries = db.collection(UserFulltext.COLLECTION)
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
            val existingFriendEntries = db.collection(UserProfile.COLLECTION).document(currentUserId).collection(Friend.COLLECTION).get().await()
            for (existingFriendEntry in existingFriendEntries) {
                users.remove(existingFriendEntry.id)
            }

            return users.values.toList()
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun sendAppointment: {${e.message}")
            return null
        }
    }

    override suspend fun getNotificationsAndAppointments(authUserId: String): List<Any>? {
        return try {

            val notifications: List<Notification> =
                db.collection(UserProfile.COLLECTION).document(authUserId)
                    .collection(Notification.COLLECTION)
                    .get()
                    .await()
                    .toObjects()

            val appointments: List<Appointment> =
                db.collection(UserProfile.COLLECTION).document(authUserId)
                    .collection(Appointment.COLLECTION)
                    .whereGreaterThan("state", Appointment.STATE_I_AM_INVITED) //only if I_INVITED or ACCEPTED
                    .get()
                    .await()
                    .toObjects()

            return notifications + appointments

            // TODO not accepted invitations where authUserId == inviteeUserId should be on top, order by created time desc
            // rest should be under it order by DateTime asc
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun getAcceptedAppointments: {${e.message}")
            null
        }
    }

    override suspend fun getFriends(authUserId: String): List<Friend>? = db.collection(UserProfile.COLLECTION).document(authUserId)
        .collection(Friend.COLLECTION)
        .whereGreaterThan("state", Friend.STATE_I_AM_INVITED)
        .get()
        .awaitOrNull()
        ?.toObjects()


    override suspend fun getUser(authUserId: String): UserProfile? = db.document("${UserProfile.COLLECTION}/$authUserId")
        .get()
        .awaitOrNull()
        ?.toObject<UserProfile>()

    override suspend fun makeFriends(user1: UserProfile, user2: UserProfile): Boolean {
        try {
            return if (user1.authUserId != null && user2.authUserId != null) {
                db.collection(UserProfile.COLLECTION).document(user1.authUserId)
                    .collection(Friend.COLLECTION)
                    .document(user2.authUserId)
                    .set(user2)
                    .await() // can throw exception

                db.collection(UserProfile.COLLECTION).document(user2.authUserId)
                    .collection(Friend.COLLECTION)
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
        db.collection(UserProfile.COLLECTION).document(authUserId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc?.get(UserProfile::fullName.name) != null) {
                    onTrue()
                    return@addOnSuccessListener
                }

                onFalse()
            }
            .addOnFailureListener {
                Timber.d("databaseService.hasProfileFilled() failed")
            }
    }

    override suspend fun createUserProfile(user: UserProfile): Boolean = db.runBatch { batch ->
        val userId = user.authUserId!!

        val profile = db.collection(UserProfile.COLLECTION).document(userId)

        // I don't want to overwrite the deviceToken
        batch.set(profile, user)

        // Róbert Vereš -> robert veres
        val userNameNormalized = user.fullName.normalized()
        val fulltextUserEntry = db.collection(UserFulltext.COLLECTION).document()
        val userFulltext = UserFulltext(fulltextName = userNameNormalized, fullName = user.fullName, authUserId = userId)
        batch.set(fulltextUserEntry, userFulltext)

        // robert j. veres -> veres robert j.
        val userNameNormalizedSwapped = userNameNormalized.putLastWordFirst()

        if (userNameNormalizedSwapped != null) {
            val fulltextUserEntrySwapped = db.collection(UserFulltext.COLLECTION).document()
            batch.set(fulltextUserEntrySwapped, userFulltext.copy(fulltextName = userNameNormalizedSwapped))
        }
    }.awaitWithStatus()


    private suspend fun sendSystemPushNotification(systemNotification: SystemPushNotification) {
        try {
            val response = RetrofitInstance.api.postNotification(
                title = systemNotification.title,
                body = systemNotification.body,
                token = systemNotification.token
            )

            if (response.isSuccessful) {
                Timber.d("Response: $response")
            } else {
                val error = response.errorBody()
                Timber.e("Error: $error")
            }
        } catch (e: Exception) {
            Timber.e(e.toString())
        }
    }
}