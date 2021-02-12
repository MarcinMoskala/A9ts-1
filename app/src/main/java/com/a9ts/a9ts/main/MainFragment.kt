package com.a9ts.a9ts.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.a9ts.a9ts.R
import com.a9ts.a9ts.databinding.MainFragmentBinding
import com.a9ts.a9ts.model.AppointmentRepository
import com.a9ts.a9ts.toast

class MainFragment : Fragment() {


    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val adapter = AppointmentListAdapter(AppointmentRepository.appointmentList)
        val binding = MainFragmentBinding.inflate(layoutInflater, container, false)

        binding.viewModel = viewModel

        setHasOptionsMenu(true)


        // just testing the ViewBinding
        viewModel.showUser.observe(viewLifecycleOwner,  { user ->
            user?.let {
                toast("Username is ${user.fullName}")
                viewModel.showUserDone()
            }
        })

        viewModel.fabClicked.observe(viewLifecycleOwner, {
            if (it == true) {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToStepOneFragment())
                viewModel.fabClickedDone()
            }
        })


        return binding.run {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            root
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
            inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_write_to_db -> {
                viewModel.onMenuWriteToDb()
                return true
            }
            R.id.action_about -> {
                viewModel.onMenuAbout()
                return true
            }

            R.id.befriend_marcin_and_igor -> {
                viewModel.onMenuBefriendMarcinAndIgor()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}