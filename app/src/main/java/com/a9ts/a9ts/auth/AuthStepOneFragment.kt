package com.a9ts.a9ts.auth

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.a9ts.a9ts.MainActivity
import com.a9ts.a9ts.R
import com.a9ts.a9ts.databinding.AuthStepOneFragmentBinding
import com.a9ts.a9ts.model.AuthService
import com.a9ts.a9ts.toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit




class AuthStepOneFragment : Fragment() {
    val authService: AuthService by inject()

    private var storedFullPhoneNumber = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = AuthStepOneFragmentBinding.inflate(inflater, container, false)

        (activity as MainActivity).supportActionBar?.title = "Your Phone"

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // automatic authentication without telephone number
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Timber.d("onVerificationCompleted:$credential")

                authService.signInWithPhoneAuthCredential(
                    requireActivity(),
                    credential,
                    onSuccess = {
                        Timber.d("onVerificationCompleted - Automatic sign in success.")
                        activity?.invalidateOptionsMenu()
                    },
                    onFailure = { exception ->
                        Timber.d("onVerificationCompleted - Automatic sign in failure: ${exception?.message}")
                    })
            }

            //SMS cant be sent
            override fun onVerificationFailed(e: FirebaseException) {
                Timber.w("onVerificationFailed : ${e.message}")
                if (e is FirebaseAuthInvalidCredentialsException) {
                    binding.editTextPhoneNumber.error = getString(R.string.invalid_phone_number)
                } else if (e is FirebaseTooManyRequestsException) {
                    Timber.d(getString(R.string.quota_exceeded))
                }

                binding.progressBar.visibility = View.INVISIBLE
                binding.buttonGetSmsCode.isEnabled =  true
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                toast("SMS code sent to: $storedFullPhoneNumber")

                findNavController().navigate(
                    AuthStepOneFragmentDirections.actionAuthStepOneFragmentToAuthStepTwoFragment(
                        verificationId,
                        storedFullPhoneNumber
                    )
                )
            }
        }


        binding.apply {
            editTextPhoneNumber.requestFocus()
            buttonGetSmsCode.setOnClickListener { startPhoneNumberVerification(this, callbacks) }
        }

        return binding.root
    }

    private fun startPhoneNumberVerification(binding: AuthStepOneFragmentBinding, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        val phoneNumber = binding.editTextPhoneNumber.text.toString().trim()
        val countryCode = binding.editTextCountryCode.text.toString().trim()

        when {
            TextUtils.isEmpty(phoneNumber) -> {
                binding.editTextPhoneNumber.error = "Phone number is required!"
                binding.editTextPhoneNumber.requestFocus()
            }
            TextUtils.isEmpty(countryCode) -> {
                binding.editTextCountryCode.error = "Country code is required!"
                binding.editTextCountryCode.requestFocus()
            }
            else -> {
                var fullPhoneNumber = countryCode + phoneNumber

                if (!fullPhoneNumber.startsWith("+")) {
                    fullPhoneNumber = "+$fullPhoneNumber"
                }

                toast("Submitting $fullPhoneNumber")

                val options = PhoneAuthOptions.newBuilder(authService.getAuth())
                    .setPhoneNumber(fullPhoneNumber)   // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(requireActivity())    // Activity (for callback binding)
                    .setCallbacks(callbacks)           // OnVerificationStateChangedCallbacks
                    .build()

                binding.progressBar.visibility = View.VISIBLE
                binding.buttonGetSmsCode.isEnabled =  false

                storedFullPhoneNumber = fullPhoneNumber

                PhoneAuthProvider.verifyPhoneNumber(options)

            }
        }
    }

}