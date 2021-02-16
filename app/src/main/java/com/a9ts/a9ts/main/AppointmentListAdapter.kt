package com.a9ts.a9ts.main

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a9ts.a9ts.databinding.AppointmentItemBinding
import com.a9ts.a9ts.databinding.NotificationItemBinding
import com.a9ts.a9ts.model.Appointment
import timber.log.Timber


class AppointmentListAdapter(
    private val appointments: List<Appointment>,
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
        if (appointments[position].inviteeUserId == authUserId
            && appointments[position].accepted == null) {
            return NOTIFICATION
        } else {
            return APPOINTMENT
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            APPOINTMENT ->
                (holder as AppointmentViewHolder).bind(appointments[position], authUserId)
            NOTIFICATION ->
                (holder as NotificationViewHolder).bind(appointments[position], authUserId)
        }
    }


    override fun getItemCount() = appointments.size

    class NotificationViewHolder(private val itemBinding: NotificationItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(appointment: Appointment, authUserId: String) {
            itemBinding.apply {

            }
        }

        init {
            itemBinding.root.setOnClickListener {
                Timber.d("Click")
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


//class AppointmentListAdapter(private val appointments:List<Appointment>, private val authUserId: String) : RecyclerView.Adapter<AppointmentListAdapter.ViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        return AppointmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//            .let(::ViewHolder)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//            holder.bind(appointments[position], authUserId)
//        }
//
//
//    override fun getItemCount() = appointments.size
//
//    class ViewHolder(private val itemBinding : AppointmentItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
//
//        fun bind(appointment: Appointment, authUserId: String) {
//            itemBinding.apply {
//
//                if (authUserId == appointment.inviteeUserId) {
//                    fullname.text = appointment.invitorName
//                } else {
//                    fullname.text = appointment.inviteeName
//                }
//
//                val date = appointment.dateAndTime!!.toDate()
//
//                dateTextView.text = DateFormat.format("E dd LLL",date)
//                timeTextView.text = DateFormat.format("HH:mm",date)
//                waitingToAcceptTextView.visibility = if (appointment.accepted == null) View.VISIBLE else View.GONE
//            }
//        }
//
//        init {
//            itemBinding.root.setOnClickListener {
//                Timber.d("Click")
//            }
//        }
//    }
//
//}


