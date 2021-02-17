package com.a9ts.a9ts.main

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a9ts.a9ts.databinding.AppointmentItemBinding
import com.a9ts.a9ts.databinding.NotificationItemBinding
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.model.NOTIFICATION_TYPE_INVITATION
import com.a9ts.a9ts.model.Notification
import timber.log.Timber


class AppointmentListAdapter(
    private val appointments: List<Any>,
    private val authUserId: String

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val APPOINTMENT = 1
        const val NOTIFICATION = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            APPOINTMENT -> AppointmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                .let(::AppointmentViewHolder)
            NOTIFICATION -> NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                .let(::NotificationViewHolder)
            else -> throw IllegalArgumentException("Unexpected ViewHolderType")
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (appointments[position] is Notification) {
            NOTIFICATION
        } else {
            APPOINTMENT
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            APPOINTMENT ->
                (holder as AppointmentViewHolder).bind(appointments[position] as Appointment, authUserId)
            NOTIFICATION ->
                (holder as NotificationViewHolder).bind(appointments[position] as Notification, authUserId)
        }
    }


    override fun getItemCount() = appointments.size

    class NotificationViewHolder(private val itemBinding: NotificationItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(notification: Notification, authUserId: String) {
            itemBinding.apply {
                if (notification.notificationType == NOTIFICATION_TYPE_INVITATION) {
                    itemBinding.headingTextView.text = "Invitation from " + notification.fullName
                }

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

    class AppointmentViewHolder(private val itemBinding: AppointmentItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(appointment: Appointment, authUserId: String) {
            itemBinding.apply {

                if (authUserId == appointment.inviteeUserId) {
                    fullname.text = appointment.invitorName
                } else {
                    fullname.text = appointment.inviteeName
                }

                val date = appointment.dateAndTime!!.toDate()

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





