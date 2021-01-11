package com.a9ts.a9ts.auth

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.a9ts.a9ts.AuthActivity
import com.a9ts.a9ts.AuthActivity.Companion.TAG
import com.a9ts.a9ts.R
import com.a9ts.a9ts.databinding.AuthStepOneFragmentBinding
import com.a9ts.a9ts.model.FirebaseAuthService
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import org.jetbrains.anko.support.v4.toast
import java.util.concurrent.TimeUnit


class AuthStepOneFragment() : Fragment() {
    private lateinit var binding: AuthStepOneFragmentBinding
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    // I don't like this here, seems off - but not sure how to do thing requiring activity in Fragments ohterwise
    private lateinit var parentActivity: AuthActivity

    private var storedFullPhoneNumber = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = AuthStepOneFragmentBinding.inflate(inflater, container, false)
        binding.editTextPhoneNumber.requestFocus()
        binding.buttonGetSmsCode.setOnClickListener { startPhoneNumberVerification() }

        parentActivity = (activity as AuthActivity)

        // not sure why I can't just use 'activity'
        parentActivity.supportActionBar?.title = "Your Phone"
        parentActivity.supportActionBar?.setDisplayHomeAsUpEnabled(false)


        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // automatic authentication without telephone number
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Log.d(TAG, "onVerificationCompleted:$credential")

                FirebaseAuthService.signInWithPhoneAuthCredential(
                    parentActivity, // not sure why I can't just use 'activity'
                    credential,
                    onSuccess = {
                        Log.d(TAG, "onVerificationCompleted - Automatic sign in success.")
                    },
                    onFailure = { exception ->
                        Log.d(
                            TAG,
                            "onVerificationCompleted - Automatic sign in failure: ${exception.message}"
                        )
                    })
            }

            //SMS cant be sent
            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
                if (e is FirebaseAuthInvalidCredentialsException) {
                    binding.editTextPhoneNumber.error = getString(R.string.invalid_phone_number)
                } else if (e is FirebaseTooManyRequestsException) {
                    toast(R.string.quota_exceeded)
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(AuthActivity.TAG, "onCodeSent:$verificationId")

                toast("SMS Code sent: ${verificationId}. Phone number: ${storedFullPhoneNumber}")


                this@AuthStepOneFragment.findNavController().navigate(
                    AuthStepOneFragmentDirections.actionAuthStepOneFragmentToAuthStepTwoFragment(
                        verificationId,
                        storedFullPhoneNumber
                    )
                )
            }
        }

        return binding.root
    }

    private fun startPhoneNumberVerification() {
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

                val options = PhoneAuthOptions.newBuilder(FirebaseAuthService.auth)
                    .setPhoneNumber(fullPhoneNumber)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(parentActivity)                 // Activity (for callback binding)
                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                    .build()

                storedFullPhoneNumber = fullPhoneNumber

                PhoneAuthProvider.verifyPhoneNumber(options)

            }
        }
    }


}