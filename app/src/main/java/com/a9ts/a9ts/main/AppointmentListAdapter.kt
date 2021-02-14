package com.a9ts.a9ts.main

import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.databinding.AppointmentItemBinding
import com.google.firebase.auth.ktx.oAuthCredential
import timber.log.Timber

class AppointmentListAdapter(private val appointments:List<Appointment>, val authUserId: String) : RecyclerView.Adapter<AppointmentListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return AppointmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::ViewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(appointments[position], authUserId)
        }


    override fun getItemCount() = appointments.size

    class ViewHolder(private val itemBinding : AppointmentItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(appointment: Appointment, authUserId: String) {
            itemBinding.apply {

                if (authUserId == appointment.inviteeUserId) {
                    fullname.text = appointment.invitorName
                } else {
                    fullname.text = appointment.inviteeName
                }

                val date = appointment.dateAndTime!!.toDate()

                dateTextView.text = DateFormat.format("E dd LLL",date)
                timeTextView.text = DateFormat.format("HH:mm",date)
                waitingToAcceptTextView.visibility = if (appointment.accepted == null) View.VISIBLE else View.GONE
            }
        }

        init {
            itemBinding.root.setOnClickListener {
                Timber.d("Click")
            }
        }
    }

}


