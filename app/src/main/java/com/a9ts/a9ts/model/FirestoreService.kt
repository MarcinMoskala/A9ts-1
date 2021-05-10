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

    suspend fun sendAppointment(authUserId: String, friendUserId: String, dateTimeInSeconds: Long): Triple<Notification, UserProfile, UserProfile>?

    suspend fun getUser(authUserId: String): UserProfile?
    suspend fun getFriends(authUserId: String): List<Friend>?
    suspend fun getNonFriends(firstCharacters: String, currentUserId: String): List<Friend>?

    suspend fun sendFriendInvite(userId: String, friendUserId: String) : Pair<UserProfile, UserProfile>?

    suspend fun acceptFriendInvite(acceptingUserId: String, friendUserId: String, notificationId: String): Boolean
    suspend fun rejectFriendInvite(acceptingUserId: String, friendUserId: String, notificationId: String): Boolean
    suspend fun acceptAppointmentInvitation(authUserId: String, invitorUserId: String?, appointmentId: String?, notificationId: String?): Boolean
    suspend fun rejectAppointmentInvitation(authUserId: String, invitorUserId: String?, appointmentId: String?, notificationId: String?): Boolean

    suspend fun cancelAppointmentRequest(authUserId: String, appointment: Appointment): Boolean
    suspend fun getUserDeviceToken(userId: String): String?

    fun getNotificationsListener(authUserId: String, onSuccess: (List<Notification>) -> Unit)
    fun getAppointmentsListener(authUserId: String, onSuccess: (List<Appointment>) -> Unit)
    fun updateDeviceToken(authUserId: String, deviceToken: String?, onSuccess: (deviceToken: String) -> Unit)
    fun getAppointmentListener(appointmentId: String, authUserId: String, onSuccess: (appointment: Appointment?) -> Unit)
    suspend fun acceptAppointmentCancellation(authUserId: String, appPartnerId: String, appointmentId: String, notificationId: String): Boolean
}

class FirestoreService : DatabaseService {
    private val db = Firebase.firestore

    override suspend fun acceptAppointmentCancellation(authUserId: String, appPartnerId: String, appointmentId: String, notificationId: String): Boolean {

        // by design appointments maju same ID ak ide o rovnaky appointment
        val myAppointment = db.collection(UserProfile.COLLECTION).document(authUserId).collection(Appointment.COLLECTION).document(appointmentId)
        val appPartnerAppointment = db.collection(UserProfile.COLLECTION).document(appPartnerId).collection(Appointment.COLLECTION).document(appointmentId)

        val notification = db.collection(UserProfile.COLLECTION).document(authUserId).collection(Notification.COLLECTION).document(notificationId.toString())

        return try {
            db.runTransaction { transaction ->
                transaction.delete(myAppointment)
                transaction.delete(appPartnerAppointment)
                transaction.delete(notification)
                //TODO mozno pridat este notification aby som sa ja dozvedel ze moja cancellation bola prijata... iba nieco v zmysle ze bola prijata a Appointment deleted
            }.await()
            true

        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun acceptAppointmentCancellation: {${e.message}")
            false
        }
    }

    override fun getAppointmentListener(appointmentId: String, authUserId: String, onSuccess: (appointment: Appointment?) -> Unit) {
        db.collection(UserProfile.COLLECTION).document(authUserId).collection(Appointment.COLLECTION).document(appointmentId)
            .addSnapshotListener { appointmentSnapshot, e ->
                if (e != null) {
                    Timber.w("getAppointmentListener failed: $e")
                    return@addSnapshotListener
                }

                var appointment: Appointment? = null

                if (appointmentSnapshot != null) {
                    appointment = appointmentSnapshot.toObject()
                }

                onSuccess(appointment)
            }
    }

    override suspend fun cancelAppointmentRequest(authUserId: String, appointment: Appointment): Boolean {

        try {
            val (notificationReceiverFullName, notificationReceiverId) = db.runTransaction { transaction ->

                val appointmentInvitor = db.collection(UserProfile.COLLECTION).document(appointment.invitorUserId).collection(Appointment.COLLECTION).document(appointment.id!!)
                val appointmentInvitee = db.collection(UserProfile.COLLECTION).document(appointment.inviteeUserId).collection(Appointment.COLLECTION).document(appointment.id)

                val notification = Notification(
                    notificationType = Notification.TYPE_CANCELLATION,
                    authUserId = authUserId,
                    appointmentId = appointment.id,
                    dateAndTime = appointment.dateAndTime
                )

                val notificationReceiverId: String

                if (authUserId == appointment.invitorUserId) { // I'm invitor and canceling
                    transaction.update(appointmentInvitor, Appointment::canceledByInvitor.name, FieldValue.serverTimestamp())
                    transaction.update(appointmentInvitee, Appointment::canceledByInvitor.name, FieldValue.serverTimestamp())

                    val inviteeNotification = db.collection(UserProfile.COLLECTION).document(appointment.inviteeUserId).collection(Notification.COLLECTION).document()

                    notification.fullName = appointment.invitorName
                    notificationReceiverId = appointment.inviteeUserId

                    transaction.set(inviteeNotification, notification)
                } else { // I'm invitee and canceling
                    transaction.update(appointmentInvitor, Appointment::canceledByInvitee.name, FieldValue.serverTimestamp())
                    transaction.update(appointmentInvitee, Appointment::canceledByInvitee.name, FieldValue.serverTimestamp())

                    val invitorNotification = db.collection(UserProfile.COLLECTION).document(appointment.invitorUserId).collection(Notification.COLLECTION).document()

                    notification.fullName = appointment.inviteeName
                    notificationReceiverId = appointment.invitorUserId

                    transaction.set(invitorNotification, notification)
                }

                Pair(notification.fullName, notificationReceiverId)
            }.await()

            val dateAndTime = dateAndTimeFormatted(appointment.dateAndTime.toDate())

            SystemPushNotification(
                title = "Appointment cancelled by $notificationReceiverFullName",
                body = dateAndTime,
                token = getUserDeviceToken(notificationReceiverId).toString()
            ).also { sendSystemPushNotification(it) }

            return true

        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun acceptFriendInvite: {${e.message}")

            return false
        }
    }

    override suspend fun getUserDeviceToken(userId: String): String? = try {
        val user: UserProfile? = db.collection(UserProfile.COLLECTION)
            .document(userId)
            .get()
            .await()
            .toObject()

        user?.deviceToken
    } catch (e: FirebaseFirestoreException) {
        Timber.e("suspend fun getUserDeviceToken: {${e.message}")
        null
    }

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

    override suspend fun sendAppointment(authUserId: String, friendUserId: String, utcDateTimeInSeconds: Long): Triple<Notification, UserProfile, UserProfile>? =
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

            val returnData = db.runTransaction { transaction ->
                val authUser: UserProfile? = transaction.get(authUserDoc).toObject()
                val authUserFullName = authUser?.fullName!!

                val friendUser: UserProfile? = transaction.get(friendUserDoc).toObject()
                val friendUserFullName = friendUser?.fullName!!

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

            returnData
        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun sendAppointment: {${e.message}")
            null
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

    override suspend fun sendFriendInvite(userId: String, friendUserId: String): Pair<UserProfile, UserProfile>? {
        val userProfileDoc = db.collection(UserProfile.COLLECTION).document(userId)
        val friendUserDocProfile = db.collection(UserProfile.COLLECTION).document(friendUserId)
        val friendInvitationNotification = db.collection(UserProfile.COLLECTION).document(friendUserId)
            .collection(Notification.COLLECTION).document()


        //Edge case:
        // ak uz mam zapis s tym friendom vo Friends tak return false (lebo tym padom sme friends, uz som ho pozval, alebo on pozval uz mna
        val iAmInvitedAlready = db.collection(UserProfile.COLLECTION).document(userId).collection(Friend.COLLECTION).document(friendUserId).get().await()


        if (iAmInvitedAlready.exists()) return null

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

            Pair(user, friendUser)

            //TODO moze sa stat ze uspesne zapise ale neuspesne posle systemNotification...Nie je uplny pruser, ale stoji za zamyslenie

        } catch (e: FirebaseFirestoreException) {
            Timber.e("suspend fun sendFriendInvite: {${e.message}")
            null
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



}