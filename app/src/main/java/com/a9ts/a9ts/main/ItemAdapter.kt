package com.a9ts.a9ts.main

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.a9ts.a9ts.R
import com.a9ts.a9ts.databinding.AppointmentItemBinding
import com.a9ts.a9ts.databinding.NotificationFriendInvitationItemBinding
import com.a9ts.a9ts.databinding.NotificationNewAppointmentItemBinding
import com.a9ts.a9ts.dateAndTimeFormatted
import com.a9ts.a9ts.appointmentWith
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.model.Notification
import timber.log.Timber

// General classes, used by all recycler views

// We use item layoutId as view type, so it needs to be unique
abstract class GeneralAdapter(items: List<ItemAdapter>) : RecyclerView.Adapter<BaseViewHolder>() {
    private val items = items.toMutableList()

    final override fun getItemCount() = items.size

    final override fun getItemViewType(position: Int) = items[position].layoutId

    final override fun onCreateViewHolder(parent: ViewGroup, layoutId: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return BaseViewHolder(view)
    }

    final override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        items[position].setupView(holder)
    }

    fun removeItem(holder: BaseViewHolder) {
        // TODO remove by NotificationID and AppointmentID
        val position = holder.adapterPosition
        items.removeAt(position)
        notifyItemRemoved(position)
    }

}

abstract class ItemAdapter(@LayoutRes open val layoutId: Int) {
    abstract fun setupView(holder: BaseViewHolder)
}

class BaseViewHolder(val view: View) : RecyclerView.ViewHolder(view)

class ItemListAdapter(items: List<ItemAdapter>) : GeneralAdapter(items)

class AppointmentItemAdapter(
    private val appointment: Appointment,
    private val authUserId: String,
    private val onClick: () -> Unit
) : ItemAdapter(R.layout.appointment_item) {

    override fun setupView(holder: BaseViewHolder) {
        val binding = AppointmentItemBinding.bind(holder.view)

        val appointmentWithFullName = appointmentWith(authUserId, appointment.invitorUserId, appointment.invitorName, appointment.inviteeName)

        binding.fullname.text = appointmentWithFullName

        val date = appointment.dateAndTime.toDate()

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
            onClick()
        }
    }
}

class NotificationFriendInvitationItemAdapter(
    private val notification: Notification,
    private val onAccept: (holder: BaseViewHolder) -> Unit,
    private val onReject: (holder: BaseViewHolder) -> Unit

) : ItemAdapter(R.layout.notification_friend_invitation_item) {

    override fun setupView(holder: BaseViewHolder) {
        val binding = NotificationFriendInvitationItemBinding.bind(holder.view)
        binding.headingTextView.text = "Friend invitation from: " + notification.fullName

        binding.yesButton.setOnClickListener {
            onAccept(holder)
            Timber.d("Approved friend invite!")
        }

        binding.noButton.setOnClickListener {
            onReject(holder)
            Timber.d("Rejected friend invite!")
        }
    }
}


class NotificationNewAppointmentItemAdapter(
    private val notification: Notification,
    private val onAccept: (holder: BaseViewHolder) -> Unit,
    private val onReject: (holder: BaseViewHolder) -> Unit
) : ItemAdapter(R.layout.notification_new_appointment_item) {

    override fun setupView(holder: BaseViewHolder) {
        val binding = NotificationNewAppointmentItemBinding.bind(holder.view)

        binding.headingTextView.text = "Appointment invitation from: " + notification.fullName



        binding.dateTimeTextView.text = dateAndTimeFormatted(notification.dateAndTime!!.toDate())

        binding.yesButton.setOnClickListener {
            onAccept(holder)
        }

        binding.noButton.setOnClickListener {
            onReject(holder)
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
