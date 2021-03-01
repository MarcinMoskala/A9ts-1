package com.a9ts.a9ts.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.a9ts.a9ts.R
import com.a9ts.a9ts.databinding.MainFragmentBinding
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.model.Notification
import com.a9ts.a9ts.toast

class MainFragment : Fragment() {


    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val binding = MainFragmentBinding.inflate(layoutInflater, container, false)

        binding.viewModel = viewModel

        setHasOptionsMenu(true)

        // DONE askmarcin - I'm starting the app on this fragment and want to check if user is logged
        // Not sure if this is the best way how to do it
        viewModel.notLoggedEvent.observe(viewLifecycleOwner, { notLogged ->
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToAuthStepOneFragment())
        })

        viewModel.showUser.observe(viewLifecycleOwner, { user ->
            user?.let {
                toast("ID: ${user.authUserId}\nName: ${user.fullName}\nPhone: ${user.telephone}")
                viewModel.showUserDone()
            }
        })

        viewModel.fabClicked.observe(viewLifecycleOwner, {
            if (it == true) {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToStepOneFragment())
                viewModel.fabClickedDone()
            }

        })

        viewModel.friendNotificationAccepted.observe(viewLifecycleOwner, { holder ->
            if (holder != null) {
                (binding.recyclerView.adapter as ItemListAdapter).removeItem(holder)
                viewModel.onFriendNotificationAcceptedDone()
            }
        })

        viewModel.friendNotificationRejected.observe(viewLifecycleOwner, { holder ->
            if (holder != null) {
                (binding.recyclerView.adapter as ItemListAdapter).removeItem(holder)
                viewModel.onFriendNotificationRejectedDone()
            }
        })

        viewModel.appointmentNotificationAccepted.observe(viewLifecycleOwner, { holder ->
            if (holder != null) {
                (binding.recyclerView.adapter as ItemListAdapter).removeItem(holder)
                viewModel.onAppointmentNotificationAcceptedDone()
            }
        })

        viewModel.appointmentNotificationRejected.observe(viewLifecycleOwner, { holder ->
            if (holder != null) {
                (binding.recyclerView.adapter as ItemListAdapter).removeItem(holder)
                viewModel.onAppointmentNotificationRejectedDone()
            }
        })


        viewModel.notificationsAndAppointments.observe(viewLifecycleOwner, { items ->
            // TODO toto mozno nema byt tu
            binding.recyclerView.layoutManager = LinearLayoutManager(context)


            //askmarcin can this be written more nicely?
            val itemListNotification: ArrayList<ItemAdapter> = ArrayList(items.mapNotNull { item ->

                if (item is Notification) {
                    val notification = item

                    if (notification.notificationType == Notification.TYPE_APP_INVITATION)
                        return@mapNotNull NotificationNewAppointmentItemAdapter(
                            notification,
                            { holder -> viewModel.onAppointmentNotificationAccepted(holder, notification.authUserId, notification.appointmentId, notification.id) },
                            { holder -> viewModel.onAppointmentNotificationRejected(holder, notification.authUserId, notification.appointmentId, notification.id) })

                    if (item.notificationType == Notification.TYPE_FRIEND_INVITATION)
                        return@mapNotNull NotificationFriendInvitationItemAdapter(
                            notification,
                            { holder -> viewModel.onFriendNotificationAccepted(holder, notification.authUserId, notification.id) },
                            { holder -> viewModel.onFriendNotificationRejected(holder, notification.authUserId, notification.id) })
                }

                if (item is Appointment) {
                    val appointment = item
                    return@mapNotNull AppointmentItemAdapter(appointment, viewModel.authUserId)
                }

                null
            })

            binding.recyclerView.adapter = ItemListAdapter(itemListNotification)
        })

        viewModel.onCreateView() //populate recyclerview

        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_about -> {
                viewModel.onMenuAbout()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}