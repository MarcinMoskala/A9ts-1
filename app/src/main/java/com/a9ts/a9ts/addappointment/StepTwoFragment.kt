package com.a9ts.a9ts.addappointment

import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.a9ts.a9ts.MainActivity
import com.a9ts.a9ts.databinding.AddAppointmentStepTwoFragmentBinding
import com.a9ts.a9ts.toast
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import timber.log.Timber
import java.time.*
import java.util.*

class StepTwoFragment: Fragment() {

    private val args: StepTwoFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val binding = AddAppointmentStepTwoFragmentBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this

//        (activity as MainActivity).supportActionBar?.title = "New appo"

        val viewModelFactory = StepTwoViewModelFactory(args.friendUserId, args.friendFullName)

        val viewModel = ViewModelProvider(this, viewModelFactory).get(StepTwoViewModel::class.java)


        // ---- DATE PICKER ------------------------------------------------------------------------
        viewModel.dateClicked.observe(viewLifecycleOwner, { dateClicked ->
            if (dateClicked == true) {
                val datePicker = MaterialDatePicker.Builder
                    .datePicker()
                    .setTitleText("Change date")
                    .build()

                datePicker.addOnPositiveButtonClickListener { dateInMilliSeconds ->
                    viewModel.onDateChanged(datePicker.headerText, dateInMilliSeconds / 1000)
                }

                datePicker.show(childFragmentManager, "date_picker_fragment")
                viewModel.onDateClickedDone()
            }
        })


        // ---- TIME PICKER ------------------------------------------------------------------------
        viewModel.timeClicked.observe(viewLifecycleOwner, { timeClicked ->
            if (timeClicked == true) {
                val isSystem24Hour = is24HourFormat(context)
                val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

                val timePicker = MaterialTimePicker.Builder()
                    .setHour(StepTwoViewModel.DEFAULT_TIME_HOURS.toInt()) //TODO
                    .setMinute(0)
                    .setTitleText("Change time")
                    .setTimeFormat(clockFormat)
                    .build()

                timePicker.addOnPositiveButtonClickListener {
                    viewModel.onTimeChanged(timePicker.hour, timePicker.minute)
                }

                timePicker.show(childFragmentManager, "time_picker_fragment")
                viewModel.onTimeClickedDone()
            }
        })

        // ---- SUBMIT BUTTON ----------------------------------------------------------------------
        viewModel.submitClicked.observe(viewLifecycleOwner, { submitClickedAndAppointmentSent ->
            if (submitClickedAndAppointmentSent == true) {
                findNavController().navigate(StepTwoFragmentDirections.actionStepTwoFragmentToMainFragment())
            }
        })

        viewModel.submitClicked.observe(viewLifecycleOwner, {
            if (it == true) {
                viewModel.onSubmitDone()
            }
        })





        binding.viewModel = viewModel

        return binding.root
    }

}