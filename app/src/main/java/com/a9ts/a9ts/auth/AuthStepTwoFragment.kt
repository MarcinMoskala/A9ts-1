package com.a9ts.a9ts.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.a9ts.a9ts.MainActivity
import com.a9ts.a9ts.databinding.AuthStepTwoFragmentBinding
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.model.DatabaseService
import com.a9ts.a9ts.toast
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import org.koin.android.ext.android.inject
import timber.log.Timber


class AuthStepTwoFragment : Fragment() {
    private val authService: AuthService by inject()
    private val databaseService: DatabaseService by inject()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val args = AuthStepTwoFragmentArgs.fromBundle(requireArguments())

        (activity as MainActivity).supportActionBar?.title = args.fullPhoneNumber


        val binding = AuthStepTwoFragmentBinding.inflate(inflater, container, false)

        binding.buttonSendCode.setOnClickListener {

            val code = binding.editTextVerificationCode.text.toString()

            if (code.isEmpty()) {
                binding.editTextVerificationCode.error = "Cannot be empty."
            } else {
                val credential = PhoneAuthProvider.getCredential(args.verificationId, code)
                val navController = this.findNavController()

                authService.signInWithPhoneAuthCredential(
                    requireActivity(),
                    credential,
                    onSuccess = {
                        (activity as MainActivity).viewModel.onUpdateDeviceToken()
                        databaseService.hasProfileFilled(
                            authService.authUserId,
                            onTrue = { // navigate to mainFragment; add Logout to menu
                                navController.navigate(AuthStepTwoFragmentDirections.actionAuthStepTwoFragmentToMainFragment())
                                activity?.invalidateOptionsMenu()
                            },
                            onFalse = { // navigate to Step 3
                                navController.navigate(AuthStepTwoFragmentDirections.actionAuthStepTwoFragmentToAuthStepThreeFragment())
                            }
                        )
                    },
                    onFailure = { exception ->
                        Timber.d("signInWithCredential:failure : ${exception?.message}")
                        if (exception is FirebaseAuthInvalidCredentialsException) {
                            binding.editTextVerificationCode.error = "Invalid code."

                            //TODO UI poriesit ked zadam zly CODE
                            toast("Sign in fail: Verification code WRONG")
                        }
                    })
            }
        }

        return binding.root
    }
}


