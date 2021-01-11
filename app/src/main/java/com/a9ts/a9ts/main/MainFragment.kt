package com.a9ts.a9ts.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.a9ts.a9ts.model.AppointmentRepository
import com.a9ts.a9ts.databinding.MainFragmentBinding

class MainFragment : Fragment() {
    lateinit var binding: MainFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = MainFragmentBinding.inflate(layoutInflater)


        val adapter = AppointmentListAdapter(AppointmentRepository.appointmentList)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter


        return binding.root
    }

}