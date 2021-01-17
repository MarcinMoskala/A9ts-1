package com.a9ts.a9ts.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.a9ts.a9ts.databinding.MainFragmentBinding
import com.a9ts.a9ts.model.AppointmentRepository
import com.a9ts.a9ts.model.FirebaseAuthService
import org.jetbrains.anko.support.v4.toast

class MainFragment : Fragment() {
    lateinit var binding: MainFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = MainFragmentBinding.inflate(layoutInflater)


        val adapter = AppointmentListAdapter(AppointmentRepository.appointmentList)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        if (FirebaseAuthService.auth.currentUser == null) {
            // TODO navigate to auth first step
        } else { // just to know if it works
            val phoneNumber = FirebaseAuthService.auth.currentUser?.phoneNumber.toString()
            toast("Welcome user: $phoneNumber")
        }

        return binding.root
    }

}