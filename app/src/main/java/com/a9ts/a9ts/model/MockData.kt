package com.a9ts.a9ts.model

import com.a9ts.a9ts.model.dataclass.Appointment
import com.a9ts.a9ts.model.dataclass.Notification
import com.google.firebase.Timestamp

val mockAppointment = Appointment(
    inviteeName = "Robert Veres",
    inviteeUserId = "Os7gzVjFkyNVYVQMiPmQLZIh8Sw2",
    invitorName = "Peter Veres",
    invitorUserId = "pujbtIPGlieNOsxCTGcQVdiR5Ob2",
    dateAndTime = Timestamp.now(),
    state = Appointment.STATE_I_INVITED
)


val mockAppointmentNotification = Notification(
    authUserId = "pujbtIPGlieNOsxCTGcQVdiR5Ob2",
    fullName = "Peter Veres",
    notificationType = Notification.TYPE_APP_INVITATION,
    dateAndTime = Timestamp.now()
)


val mockFriendNotification = Notification(
    authUserId = "pujbtIPGlieNOsxCTGcQVdiR5Ob2",
    fullName = "Igor Križko",
    notificationType = Notification.TYPE_FRIEND_INVITATION
)
