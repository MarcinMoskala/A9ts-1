package com.a9ts.a9ts.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp


data class Appointment(
    @DocumentId val id : String? = null, //null aby som ho mohol ignorovat pri zapise a vlozil ID sam za mna
    val dateAndTime: Timestamp = Timestamp.now(),
    val invitorName: String = "",
    val inviteeName: String = "",
    val invitorUserId: String = "",
    val inviteeUserId: String = "",
    val state: Int = -1,

    @ServerTimestamp val created: Timestamp? = null, //musim mat moznost dat null, lebo chcem poslat "null" aby vyplnil timestamp namiesto mna
    val accepted: Timestamp? = null,
    val canceledByInvitor: Timestamp? = null,
    val canceledByInvitee: Timestamp? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(Timestamp::class.java.classLoader)!!, //askmarin is this OK? "!!"
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readParcelable(Timestamp::class.java.classLoader)
    ) {
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeParcelable(dateAndTime, flags)
        parcel.writeString(invitorName)
        parcel.writeString(inviteeName)
        parcel.writeString(invitorUserId)
        parcel.writeString(inviteeUserId)
        parcel.writeInt(state)
        parcel.writeParcelable(created, flags)
        parcel.writeParcelable(accepted, flags)
        parcel.writeParcelable(canceledByInvitor, flags)
        parcel.writeParcelable(canceledByInvitee, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Appointment> {
        override fun createFromParcel(parcel: Parcel): Appointment {
            return Appointment(parcel)
        }

        override fun newArray(size: Int): Array<Appointment?> {
            return arrayOfNulls(size)
        }

        const val STATE_I_AM_INVITED = 0
        const val STATE_I_INVITED = 1
        const val STATE_ACCEPTED = 2
        const val COLLECTION = "appointment"

    }
}