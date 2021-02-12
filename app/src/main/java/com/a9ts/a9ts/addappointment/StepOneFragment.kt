package com.a9ts.a9ts.addappointment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.a9ts.a9ts.MainActivity
import com.a9ts.a9ts.databinding.AddAppointmentStepOneFragmentBinding
import com.a9ts.a9ts.toast

class StepOneFragment : Fragment() {
    private val viewModel: StepOneViewModel by lazy {
        ViewModelProvider(this).get(StepOneViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val binding = AddAppointmentStepOneFragmentBinding.inflate(layoutInflater, container, false)

        (activity as MainActivity).supportActionBar?.title = "Appointment with..."

        viewModel.myFriends.observe(viewLifecycleOwner, {friendsList ->
            if (friendsList.isNotEmpty()) {
                binding.recyclerView.adapter = FriendListAdapter(friendsList, { friendUserId, friendFullname ->
                    findNavController().navigate(StepOneFragmentDirections.actionStepOneFragmentToStepTwoFragment(friendUserId, friendFullname))
                })
            }
        })

        return binding.run {
            recyclerView.layoutManager = LinearLayoutManager(context)
            root
        }
    }
}