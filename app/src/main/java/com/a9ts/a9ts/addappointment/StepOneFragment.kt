package com.a9ts.a9ts.addappointment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.a9ts.a9ts.MainActivity
import com.a9ts.a9ts.databinding.AddAppointmentStepOneFragmentBinding
import com.a9ts.a9ts.main.ItemListAdapter

class StepOneFragment : Fragment() {
    private val viewModel: StepOneViewModel by lazy {
        ViewModelProvider(this).get(StepOneViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val binding = AddAppointmentStepOneFragmentBinding.inflate(layoutInflater, container, false)

//        (activity as MainActivity).supportActionBar?.title = "Appointment with..."

        viewModel.myFriends.observe(viewLifecycleOwner, { friendsList ->
            if (friendsList.isNotEmpty()) {
                binding.recyclerView.adapter = FriendListAdapter(
                    friendsList,
                    onClick = { friendUserId, friendFullname ->
                        findNavController().navigate(StepOneFragmentDirections.actionStepOneFragmentToStepTwoFragment(friendUserId, friendFullname))
                    },
                    onClickInvited = { friendFullName ->
                        AlertDialog.Builder(requireContext())
                            .setMessage("$friendFullName haven't accepted your invitation yet 🙁.")
                            .setPositiveButton("Okey") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    })
            }
        })

        viewModel.addFriendsClicked.observe(viewLifecycleOwner, { addFriendsClicked ->
            if (addFriendsClicked == true) {
                findNavController().navigate(StepOneFragmentDirections.actionStepOneFragmentToAddFriendsFragment())
                viewModel.onAddFriendsClickedDone()
            }
        })


        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.onCreateView() // populate recycler view

        return binding.run {
            recyclerView.layoutManager = LinearLayoutManager(context)
//          recyclerView.adapter = FriendListAdapter(listOf(), onClick = , onClickInvited = ) // askmarcin shoud I initialize it here, or have "No adapter attached" Error in the logs?
            root
        }
    }
}