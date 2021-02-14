package com.a9ts.a9ts.model

import com.google.firebase.Timestamp

object AppointmentRepository {
    const val JULY_FIRST_NOON = 1625133600L

    val sampleDataAppointmentList = listOf(
        Appointment(
            dateAndTime = Timestamp(JULY_FIRST_NOON, 0),
            invitorName = "Robert Veres",
            inviteeName = "Igor Krizko",
            invitorUserId = "n2ay6sQbEjOhTQjnupktOou1RoI3",
            inviteeUserId = "QTIFcGvQSJXLpr4pah8iOhFATyx1",
            created = null,
            accepted = null,
            canceledByInvitor = null,
            canceledByInvitee = null
        ),
        Appointment(
            dateAndTime = Timestamp(JULY_FIRST_NOON + 3600 * 24, 0),
            invitorName = "Robert Veres",
            inviteeName = "Marcin Moskala",
            invitorUserId = "n2ay6sQbEjOhTQjnupktOou1RoI3",
            inviteeUserId = "X8kEtA1z9BUMhMceRfeZJFpQqmJ2",
            created = Timestamp(JULY_FIRST_NOON - 3600 * 2, 0),
            accepted = Timestamp(JULY_FIRST_NOON + 3600 * 27, 0),
            canceledByInvitor = null,
            canceledByInvitee = null
        ),
        Appointment(
            dateAndTime = Timestamp(JULY_FIRST_NOON + 3600 * 24*6, 0),
            invitorName = "Robert Veres",
            inviteeName = "Marcin Moskala",
            invitorUserId = "n2ay6sQbEjOhTQjnupktOou1RoI3",
            inviteeUserId = "X8kEtA1z9BUMhMceRfeZJFpQqmJ2",
            created = null,
            accepted = Timestamp(JULY_FIRST_NOON + 3600 * 24*5,0),
            canceledByInvitor = null,
            canceledByInvitee = null
        )
    )

}