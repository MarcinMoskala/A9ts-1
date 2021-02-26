package com.a9ts.a9ts.main

import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.a9ts.a9ts.R
import com.a9ts.a9ts.databinding.AppointmentItemBinding
import com.a9ts.a9ts.databinding.NotificationFriendInvitationItemBinding
import com.a9ts.a9ts.databinding.NotificationNewAppointmentItemBinding
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.model.Notification
import timber.log.Timber

// General classes, used by all recycler views

// We use item layoutId as view type, so it needs to be unique
abstract class GeneralAdapter(private val items: ArrayList<ItemAdapter>) : RecyclerView.Adapter<BaseViewHolder>() {

    final override fun getItemCount() = items.size

    final override fun getItemViewType(position: Int) = items[position].layoutId

    final override fun onCreateViewHolder(parent: ViewGroup, layoutId: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return BaseViewHolder(view)
    }

    final override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        items[position].setupView(holder.view, position)
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

}

abstract class ItemAdapter(@LayoutRes open val layoutId: Int) {
    abstract fun setupView(view: View, position: Int)
}

class BaseViewHolder(val view: View) : RecyclerView.ViewHolder(view)

class ItemListAdapter(items: ArrayList<ItemAdapter>) : GeneralAdapter(items)

class AppointmentItemAdapter(private val appointment: Appointment, private val authUserId: String) : ItemAdapter(R.layout.appointment_item) {

    override fun setupView(view: View, position: Int) {
        val binding = AppointmentItemBinding.bind(view)

        if (authUserId == appointment.inviteeUserId) {
            binding.fullname.text = appointment.invitorName
        } else {
            binding.fullname.text = appointment.inviteeName
        }

        val date = appointment.dateAndTime!!.toDate()

        binding.dateTextView.text = DateFormat.format("E dd LLL", date)
        binding.timeTextView.text = DateFormat.format("HH:mm", date)

//        binding.date.text =
//            appointment.dateAndTime.format(DateTimeFormatter.ofPattern("E dd LLL")).toString()
//        binding.time.text =
//            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(appointment.dateAndTime)
//                .toString()


        binding.waitingToAcceptTextView.visibility =
            if (appointment.accepted == null) View.VISIBLE else View.GONE

        binding.root.setOnClickListener {
            Log.i("AppointmentItemAdapter", "Clicked on appointment")
        }
    }
}

class NotificationFriendInvitationItemAdapter(
    private val notification: Notification,
    private val onAccept: (position: Int) -> Unit,
    private val onReject: (position: Int) -> Unit

    ) : ItemAdapter(R.layout.notification_friend_invitation_item) {

    override fun setupView(view: View, position: Int) {
        val binding = NotificationFriendInvitationItemBinding.bind(view)
        binding.headingTextView.text = "Friend invitation from: " + notification.fullName

        binding.yesButton.setOnClickListener {
            onAccept(position)
            Timber.d("Approved friend invite!")
        }

        binding.noButton.setOnClickListener {
            onReject(position)
            Timber.d("Rejected friend invite!")
        }
    }
}


class NotificationNewAppointmentItemAdapter(
    private val notification: Notification/*,
    private val onYesClicked: (Notification)->Unit,
    private val onNoClicked: (Notification)->Unit,*/
) : ItemAdapter(R.layout.notification_new_appointment_item) {

    override fun setupView(view: View, position: Int) {
        val binding = NotificationNewAppointmentItemBinding.bind(view)

        binding.headingTextView.text = "Appointment invitation from: " + notification.fullName

        val date = notification.dateAndTime!!.toDate()

        val dateText = DateFormat.format("E dd LLL", date).toString()
        val timeText = DateFormat.format("HH:mm", date).toString()

        binding.dateTimeTextView.text = dateText.plus(" ").plus(timeText)

        binding.yesButton.setOnClickListener {
            Timber.d("Approve appointment!")
//            onYesClicked(notification)
        }

        binding.noButton.setOnClickListener {
            Timber.d("Cancel appointment!")
        }
    }
}
// You need something like that in the activity/fragment where you use it

/*fun showAppointmentInfo(appointmentInfo: AppointmentInfo) {
    class User(val name: String, val surname: String, val time: LocalDateTime)

    val abs = listOf(User("Marcin", "Michalczuk", LocalDateTime.now()))
    abs.sortedWith(compareBy({ it.surname }, { it.name }))

    val rejectedAppointmentsAdapters = appointmentInfo.rejectedAppointments.map { AppointmentRejectedInfoItemAdapter(it) }
    val appointmentsAdapters = appointmentInfo.appointments.map { AppointmentItemAdapter(it, authUserId) }
    val itemAdapters = rejectedAppointmentsAdapters + appointmentsAdapters
    binding.list.adapter = ItemListAdapter(itemAdapters)
}*/
