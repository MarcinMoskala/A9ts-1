package com.a9ts.a9ts.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.a9ts.a9ts.components.AppointmentDetail
import com.a9ts.a9ts.ui.A9tsTheme

class DetailFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val args = DetailFragmentArgs.fromBundle(requireArguments())
        val appointmentId = args.appointmentId
        val viewModelFactory = DetailViewModelFactory(appointmentId)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(DetailViewModel::class.java)

        return ComposeView(requireContext()).apply {

            setContent() {
                val scaffoldState = rememberScaffoldState()

                A9tsTheme {
                    Scaffold (
                        scaffoldState = scaffoldState
                    ) { // content
                        // askmarcin where should I put the authUserId? I would like to have it accessible in the whole Activity... not sure if it's ok
//                        AppointmentDetail(viewModel, viewModel.authUserId)
                    }
                }
            }
        }
    }
}

//@Composable
//@Preview
//fun PreviewAppointmentDetail() {
//    AppointmentDetail("Fri 02 Apr at 23:00", "Barack Obama")
//}