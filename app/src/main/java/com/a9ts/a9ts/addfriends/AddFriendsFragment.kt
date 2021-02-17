package com.a9ts.a9ts.addfriends

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.a9ts.a9ts.MainActivity
import com.a9ts.a9ts.databinding.AcitvityMainBinding.inflate
import com.a9ts.a9ts.databinding.AddAppointmentStepOneFragmentBinding
import com.a9ts.a9ts.databinding.AddFriendsFragmentBinding
import com.a9ts.a9ts.toast

class AddFriendsFragment : Fragment() {
    private val viewModel: AddFriendsViewModel by lazy {
        ViewModelProvider(this).get(AddFriendsViewModel::class.java)
    }


    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            toast(s.toString())
            viewModel.onTextChanged(s)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val binding = AddFriendsFragmentBinding.inflate(layoutInflater, container, false)

        (activity as MainActivity).supportActionBar?.title = "Add friends..."


        binding.searchNameEditText.addTextChangedListener(textWatcher)

        viewModel.newFriendsList.observe(viewLifecycleOwner, {userList->
            if (userList != null) {
                binding.recyclerView.adapter = AddFriendsListAdapter(userList)
            }
        })

        return binding.run {
            recyclerView.layoutManager = LinearLayoutManager(context)
            root
        }
    }
}