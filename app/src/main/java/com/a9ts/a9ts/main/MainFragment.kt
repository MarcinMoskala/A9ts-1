package com.a9ts.a9ts.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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

    override fun onCreate(savedInstanceState: Bundle?) {

        if (!viewModel.isLogged()) {
            findNavController().apply {
                popBackStack(R.id.mainFragment, true);
                navigate(R.id.authStepOneFragment)
            }
        }

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val adapter = AppointmentListAdapter(AppointmentRepository.appointmentList)
        val binding = MainFragmentBinding.inflate(layoutInflater, container, false)

        setHasOptionsMenu(true);

        binding.fab.setOnClickListener {
            viewModel.createUserProfile()
        }

        viewModel.showUser.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                toast("Username is ${user.fullName}")
                viewModel.showUserDone()
            }
        })


        return binding.run {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            root
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (viewModel.isLogged()) {
            inflater.inflate(R.menu.menu_main, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_write_to_db -> {
                viewModel.fillDatabaseWithData()
                return true
            }
            R.id.action_about -> {
                viewModel.showUser()
                return true
            }

            R.id.befriend_marcin_and_igor -> {
                viewModel.befriendMarcinAndIgor()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}