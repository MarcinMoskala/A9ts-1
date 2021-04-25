package com.a9ts.a9ts.main

//askmarcin why do I have to import .R explicitly? .* seems not to be enough

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.a9ts.a9ts.*
import com.a9ts.a9ts.R
import com.a9ts.a9ts.components.AppointmentsList
import com.a9ts.a9ts.components.NotificationBox
import com.a9ts.a9ts.components.NotificationsList
import com.a9ts.a9ts.model.Appointment
import com.a9ts.a9ts.model.Notification
import com.a9ts.a9ts.model.mockAppointmentNotification
import com.a9ts.a9ts.ui.A9tsTheme
import com.a9ts.a9ts.ui.BgGrey
import com.a9ts.a9ts.ui.LightGrey
import kotlinx.coroutines.launch


class MainFragment : Fragment() {
    val viewModel by viewModels<MainFragmentViewModel>()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.menu_main, menu)
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_about -> {
//                viewModel.onAboutUser()
//                return true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        setHasOptionsMenu(true)

        return ComposeView(requireContext()).apply {
            setContent {
                A9tsTheme {}
            }
        }
    }
}




