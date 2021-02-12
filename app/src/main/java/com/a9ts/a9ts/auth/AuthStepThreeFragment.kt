package com.a9ts.a9ts.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.a9ts.a9ts.MainActivity
import com.a9ts.a9ts.databinding.AuthStepThreeFragmentBinding
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.model.User
import com.a9ts.a9ts.toast
import org.koin.android.ext.android.inject

class AuthStepThreeFragment : Fragment() {
    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (activity as MainActivity).supportActionBar?.title = "Profile"

        val binding = AuthStepThreeFragmentBinding.inflate(inflater, container, false)
        binding.editTextYourName.requestFocus();
        binding.buttonDone.setOnClickListener {
            val fullName = binding.editTextYourName.text.toString().trim()

            if (fullName.isEmpty()) {
                binding.editTextYourName.error = "Name is required"
                binding.editTextYourName.requestFocus()
            } else {
                databaseService.createUserProfile(
                    User(authService.authUserId, fullName, authService.getPhoneNumber()),
                    success = {
                        toast("Fullname: '$fullName' Tel.: ${authService.getPhoneNumber()}")

                        val navController = findNavController()

                        navController.navigate(AuthStepThreeFragmentDirections.actionAuthStepThreeFragmentToMainFragment())
                    },
                    failure = { exception ->
                        toast("Error writing document: ${exception.message}")
                    }
                )
            }
        }

        return binding.root
    }
}