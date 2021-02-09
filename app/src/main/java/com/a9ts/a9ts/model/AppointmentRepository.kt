package com.a9ts.a9ts.model

import java.time.LocalDateTime

object AppointmentRepository {
    val appointmentList = listOf(
        Appointment(
            dateAndTime = LocalDateTime.of(2021, 1, 16, 19, 10, 0),
            description = "Let's play Apex",
            invitorName = "Robert Veres",
            inviteeName = "Igor Krizko",
            created = LocalDateTime.of(2021, 1, 4, 19, 0),
            accepted = null,
            canceledByInvitor = null,
            canceledByInvitee = null
        ),
        Appointment(
            dateAndTime = LocalDateTime.of(2021, 1, 21, 12, 30, 0),
            description = "Kotlin coaching session",
            invitorName = "Robert Veres",
            inviteeName = "Marcin Moskala",
            created = LocalDateTime.of(2021, 1, 3, 16, 0),
            accepted = LocalDateTime.of(2021, 1, 3, 16, 32),
            canceledByInvitor = null,
            canceledByInvitee = null
        )
    )
}