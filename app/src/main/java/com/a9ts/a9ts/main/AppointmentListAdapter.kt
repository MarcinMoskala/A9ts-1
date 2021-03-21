package com.a9ts.a9ts.main

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a9ts.a9ts.databinding.AppointmentItemBinding
import com.a9ts.a9ts.databinding.NotificationFriendInvitationItemBinding
import com.a9ts.a9ts.databinding.NotificationNewAppointmentItemBinding
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.model.Notification
import timber.log.Timber


class AppointmentListAdapter(
    private val notificationsAndAppointments: List<Any>,
    private val authUserId: String

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val APPOINTMENT = 1
        const val NOTIFICATION_NEW_APPOINTMENT = 2
        const val NOTIFICATION_FRIEND_INVITATION = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            APPOINTMENT -> AppointmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                .let(::AppointmentViewHolder)
            NOTIFICATION_NEW_APPOINTMENT -> NotificationNewAppointmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                .let(::NotificationNewAppointmentViewHolder)
            NOTIFICATION_FRIEND_INVITATION -> NotificationFriendInvitationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                .let(::NotificationFriendInvitationViewHolder)
            else -> throw IllegalArgumentException("Unexpected ViewHolderType")
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (notificationsAndAppointments[position] is Notification) {
            val notification = notificationsAndAppointments[position] as Notification
            when (notification.notificationType) {
                Notification.TYPE_APP_INVITATION -> NOTIFICATION_NEW_APPOINTMENT
                Notification.TYPE_FRIEND_INVITATION -> NOTIFICATION_FRIEND_INVITATION
                else -> error("Inpossible")
            }
        } else {
            APPOINTMENT
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            APPOINTMENT ->
                (holder as AppointmentViewHolder).bind(notificationsAndAppointments[position] as Appointment, authUserId)
            NOTIFICATION_NEW_APPOINTMENT ->
                (holder as NotificationNewAppointmentViewHolder).bind(notificationsAndAppointments[position] as Notification, authUserId)
            NOTIFICATION_FRIEND_INVITATION ->
                (holder as NotificationFriendInvitationViewHolder).bind(notificationsAndAppointments[position] as Notification, authUserId)
        }
    }


    override fun getItemCount() = notificationsAndAppointments.size

    class NotificationNewAppointmentViewHolder(private val itemBinding: NotificationNewAppointmentItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(notification: Notification, authUserId: String) {
            itemBinding.apply {
                itemBinding.headingTextView.text = "Appointment invitation from: " + notification.fullName

                val date = notification.dateAndTime!!.toDate()

                val dateText = DateFormat.format("E dd LLL", date).toString()
                val timeText = DateFormat.format("HH:mm", date).toString()

                itemBinding.dateTimeTextView.text = dateText.plus(" ").plus(timeText)
            }
        }

        init {
            itemBinding.yesButton.setOnClickListener {
                Timber.d("Approve!")
                //aksmarcin how to have here a ViewModel "onMethodXY" called?
            }

            itemBinding.noButton.setOnClickListener {
                Timber.d("Cancel!")
            }

        }
    }

    class NotificationFriendInvitationViewHolder(private val itemBinding: NotificationFriendInvitationItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(notification: Notification, authUserId: String) {
            itemBinding.apply {
                itemBinding.headingTextView.text = "Friend invitation from: " + notification.fullName
            }
        }

        init {
            itemBinding.yesButton.setOnClickListener {
                Timber.d("Approve!")
                //aksmarcin how to have here a ViewModel "onMethodXY" called?
            }

            itemBinding.noButton.setOnClickListener {
                Timber.d("Cancel!")
            }

        }
    }

    class AppointmentViewHolder(private val itemBinding: AppointmentItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(appointment: Appointment, authUserId: String) {
            itemBinding.apply {

                if (authUserId == appointment.inviteeUserId) {
                    fullname.text = appointment.invitorName
                } else {
                    fullname.text = appointment.inviteeName
                }

                val date = appointment.dateAndTime.toDate()

                dateTextView.text = DateFormat.format("E dd LLL", date)
                timeTextView.text = DateFormat.format("HH:mm", date)
                waitingToAcceptTextView.visibility =
                    if (appointment.accepted == null) View.VISIBLE else View.GONE
            }
        }

        init {
            itemBinding.root.setOnClickListener {
                Timber.d("Click")
            }
        }
    }

}





