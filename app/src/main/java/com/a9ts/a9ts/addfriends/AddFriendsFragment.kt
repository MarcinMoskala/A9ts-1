package com.a9ts.a9ts.addfriends

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.a9ts.a9ts.MainActivity
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
            viewModel.onTextChanged(s)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val binding = AddFriendsFragmentBinding.inflate(layoutInflater, container, false)

//        (activity as MainActivity).supportActionBar?.title = "Add friends..."




        viewModel.newFriendsList.observe(viewLifecycleOwner, {friendList->
            if (friendList != null) {
                (binding.recyclerView.adapter as AddFriendsListAdapter).updateList(friendList)

            }
        })

        //DONE askmarcin any chance to somehow name the Pair values, not just first and second?
        viewModel.buttonClicked.observe(viewLifecycleOwner, { (buttonClicked, viewHolderPosition) ->


            if (buttonClicked) {
                val adapter = binding.recyclerView.adapter as AddFriendsListAdapter
                adapter.setButtonToSent(viewHolderPosition)
                viewModel.onButtonClickedDone()
            }
        })

        return binding.run {
            viewModel = this@AddFriendsFragment.viewModel
            lifecycleOwner = this@AddFriendsFragment

            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = AddFriendsListAdapter() { userId, viewHolderPosition ->
                this@AddFriendsFragment.viewModel.onButtonClicked(userId, viewHolderPosition)
            }

            searchNameEditText.addTextChangedListener(textWatcher)

            root
        }
    }
}