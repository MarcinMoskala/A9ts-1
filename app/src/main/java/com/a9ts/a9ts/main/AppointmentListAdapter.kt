package com.a9ts.a9ts.main

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.databinding.AppointmentItemBinding
import timber.log.Timber
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class AppointmentListAdapter(private val appointments:List<Appointment>) : RecyclerView.Adapter<AppointmentListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return AppointmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::ViewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(appointments[position])
        }


    override fun getItemCount() = appointments.size

    class ViewHolder(private val itemBinding : AppointmentItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(appointment: Appointment) {
            itemBinding.apply {
                fullname.text = appointment.inviteeName
                description.text = appointment.description
                date.text = appointment.dateAndTime.format(DateTimeFormatter.ofPattern("E dd LLL"))
                        .toString()
                time.text = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                    .format(appointment.dateAndTime).toString()
            }
        }

        init {
            itemBinding.root.setOnClickListener {
                Timber.d("Click")
            }
        }
    }

}


