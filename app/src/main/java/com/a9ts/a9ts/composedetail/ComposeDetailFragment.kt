package com.a9ts.a9ts.composedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider

class ComposeDetailFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val args = ComposeDetailFragmentArgs.fromBundle(requireArguments())
        val app = requireNotNull(activity).application
        val viewModelFactory = DetailViewModelFactory(args.appointment, app)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(DetailViewModel::class.java)


        return ComposeView(requireContext()).apply {
            setContent {
                AppointmentDetail(viewModel.dateAndTime, viewModel.appPartnerName)
            }
        }
    }
}

@Composable
fun AppointmentDetail(dateAndTime: LiveData<String>, appPartnerName: LiveData<String>) {
    val dateAndTime: String by dateAndTime.observeAsState(String())
    val appPartnerName: String by appPartnerName.observeAsState(String())

    Column() {
        Text(appPartnerName)
        Text(dateAndTime)
    }
}

//@Composable
//@Preview
//fun PreviewAppointmentDetail() {
//    AppointmentDetail("Fri 02 Apr at 23:00", "Barack Obama")
//}