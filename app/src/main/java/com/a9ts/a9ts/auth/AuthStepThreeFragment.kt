package com.a9ts.a9ts.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.a9ts.a9ts.MainActivity
import com.a9ts.a9ts.databinding.AuthStepThreeFragmentBinding


class AuthStepThreeFragment : Fragment() {


    private val viewModel: AuthStepThreeViewModel by lazy {
        ViewModelProvider(this).get(AuthStepThreeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //TODO these Titles should be done differently
//        (activity as MainActivity).supportActionBar?.title = "Profile"

        val binding = AuthStepThreeFragmentBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this // DONE askmarcin : still not sure why I need this

        binding.editTextYourName.requestFocus();

        viewModel.userProfileSubmitted.observe(viewLifecycleOwner, { userProfileSubmitted ->
            if (userProfileSubmitted == true) {
                val navController = findNavController()
                navController.navigate(AuthStepThreeFragmentDirections.actionAuthStepThreeFragmentToMainFragment())
                viewModel.onSubmitClickedDone()
            }
        })

        return binding.root
    }
}