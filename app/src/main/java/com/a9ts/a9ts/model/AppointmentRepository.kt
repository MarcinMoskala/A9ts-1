package com.a9ts.a9ts.model

import java.time.LocalDateTime

object AppointmentRepository {
    val appointmentList = listOf(
        Appointment(
            dateAndTime = LocalDateTime.of(2021, 3, 16, 19, 10, 0),
            invitorName = "Robert Veres",
            inviteeName = "Igor Krizko",
            invitorUserId = "n2ay6sQbEjOhTQjnupktOou1RoI3",
            inviteedUserId = "QTIFcGvQSJXLpr4pah8iOhFATyx1",
            created = LocalDateTime.of(2021, 3, 4, 19, 0),
            accepted = null,
            canceledByInvitor = null,
            canceledByInvitee = null
        ),
        Appointment(
            dateAndTime = LocalDateTime.of(2021, 3, 21, 12, 30, 0),
            invitorName = "Robert Veres",
            inviteeName = "Marcin Moskala",
            invitorUserId = "n2ay6sQbEjOhTQjnupktOou1RoI3",
            inviteedUserId = "X8kEtA1z9BUMhMceRfeZJFpQqmJ2",
            created = LocalDateTime.of(2021, 3, 3, 16, 0),
            accepted = LocalDateTime.of(2021, 3, 3, 16, 32),
            canceledByInvitor = null,
            canceledByInvitee = null
        )
    )
}