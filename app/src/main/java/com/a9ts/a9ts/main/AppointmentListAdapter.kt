package com.a9ts.a9ts.main

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.databinding.AppointmentItemBinding
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class AppointmentListAdapter(private val appointments:ArrayList<Appointment>) : RecyclerView.Adapter<AppointmentListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.i("AppointmentListAdapter", "ViewHolderCreated")

        val binding = AppointmentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Log.i("AppointmentListAdapter", appointments[position].toString())
            holder.bind(appointments[position])
        }


    override fun getItemCount() = appointments.size

    class ViewHolder(private val itemBinding : AppointmentItemBinding) : RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        fun bind(appointment: Appointment) {
            itemBinding.fullname.text = appointment.inviteeName
            itemBinding.description.text = appointment.description
            itemBinding.date.text = appointment.dateAndTime.format(DateTimeFormatter.ofPattern("E dd LLL")).toString()
            itemBinding.time.text = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(appointment.dateAndTime).toString()
        }

        init {
            itemBinding.root.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            Log.i("MainActivity", "Click")
        }

    }

}


