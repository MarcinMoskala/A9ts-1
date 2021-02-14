package com.a9ts.a9ts.main

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.a9ts.a9ts.R
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.databinding.AppointmentItemBinding
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// General classes, used by all recycler views

// We use item layoutId as view type, so it needs to be unique
abstract class GeneralAdapter(private val items: List<ItemAdapter>) : RecyclerView.Adapter<BaseViewHolder>() {

    final override fun getItemCount() = items.size

    final override fun getItemViewType(position: Int) = items[position].layoutId

    final override fun onCreateViewHolder(parent: ViewGroup, layoutId: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return BaseViewHolder(view)
    }

    final override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        items[position].setupView(holder.view)
    }
}

abstract class ItemAdapter(@LayoutRes open val layoutId: Int) {
    abstract fun setupView(view: View)
}

class BaseViewHolder(val view: View) : RecyclerView.ViewHolder(view)

// Classes used for appointment list

class AppointmentsListAdapter(items: List<ItemAdapter>) : GeneralAdapter(items)

class AppointmentItemAdapter(private val appointment: Appointment) : ItemAdapter(R.layout.appointment_item) {

    override fun setupView(view: View) {
        val binding = AppointmentItemBinding.bind(view)
        binding.fullname.text = appointment.inviteeName
        binding.description.text = appointment.description
        binding.date.text =
            appointment.dateAndTime.format(DateTimeFormatter.ofPattern("E dd LLL")).toString()
        binding.time.text =
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(appointment.dateAndTime)
                .toString()
        binding.root.setOnClickListener {
            Log.i("AppointmentItemAdapter", "Clicked on appointment")
        }
    }
}

class AppointmentRejectedInfoItemAdapter(private val appointment: Appointment) : ItemAdapter(R.layout.appointment_rejected_item) {

    override fun setupView(view: View) {
        val binding = AppointmentRejectedItemBinding.bind(view)
        binding.fullname.text = appointment.inviteeName
        binding.description.text = appointment.description
        binding.root.setOnClickListener {
            Log.i("AppointmentRejectedInfoItemAdapter", "Click on appointment rejected")
        }
    }
}

// You need something like that in the activity/fragment where you use it

class AppointmentInfo(
    val rejectedAppointments: List<Appointment>,
    val appointments: List<Appointment>
)

fun showAppointmentInfo(appointmentInfo: AppointmentInfo) {
    val rejectedAppointmentsAdapters = appointmentInfo.rejectedAppointments.map { AppointmentRejectedInfoItemAdapter(it) }
    val appointmentsAdapters = appointmentInfo.appointments.map { AppointmentItemAdapter(it) }
    val itemAdapters = rejectedAppointmentsAdapters + appointmentsAdapters
    binding.list.adapter = AppointmentsListAdapter(itemAdapters)
}

// That is the same as

fun showAppointmentInfo(appointmentInfo: AppointmentInfo) {
    var itemAdapters = listOf<ItemAdapter>()
    for (rejectedAppointment in appointmentInfo.rejectedAppointments) {
        itemAdapters += AppointmentRejectedInfoItemAdapter(rejectedAppointment)
    }
    for (appointment in appointmentInfo.appointments) {
        itemAdapters += AppointmentItemAdapter(appointment)
    }
    binding.list.adapter = AppointmentsListAdapter(itemAdapters)
}