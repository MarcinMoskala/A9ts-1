package com.a9ts.a9ts.model

import com.a9ts.a9ts.model.Appointment
import java.time.LocalDateTime

object AppointmentRepository {
    val appointmentList = arrayListOf(
        Appointment(
            1,
            LocalDateTime.of(2021, 1, 16, 19, 10, 0),
            "Let's play Apex",
            "Robert Veres",
            "Igor Krizko",
            LocalDateTime.of(2021, 1, 4, 19, 0),
            null,
            null,
            null
        ),
        Appointment(
            2,
            LocalDateTime.of(2021, 1, 21, 12, 30, 0),
            "Kotlin coaching session",
            "Robert Veres",
            "Marcin Moskala",
            LocalDateTime.of(2021, 1, 3, 16, 0),
            LocalDateTime.of(2021, 1, 3, 16, 32),
            null,
            null
        )
    )
}