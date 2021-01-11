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
import com.a9ts.a9ts.databinding.AuthStepTwoFragmentBinding
import com.a9ts.a9ts.model.FirebaseAuthService
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import org.jetbrains.anko.support.v4.toast


class AuthStepTwoFragment : Fragment() {
    private lateinit var binding: AuthStepTwoFragmentBinding
    private lateinit var parentActivity: AuthActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AuthStepTwoFragmentBinding.inflate(inflater, container, false)
        parentActivity = (activity as AuthActivity)

        val args = AuthStepTwoFragmentArgs.fromBundle(requireArguments())

        if (args.fullPhoneNumber.isEmpty() || args.verificationId.isEmpty()) {
            this.findNavController()
                .navigate(AuthStepTwoFragmentDirections.actionAuthStepTwoFragmentToAuthStepOneFragment())
        }

        parentActivity.supportActionBar?.title = args.fullPhoneNumber
        parentActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.editTextVerificationCode.requestFocus()

        binding.buttonSendCode.setOnClickListener {
            val code = binding.editTextVerificationCode.text.toString()

            if (TextUtils.isEmpty(code)) {
                binding.editTextVerificationCode.error = "Cannot be empty."
            } else {
                val credential = PhoneAuthProvider.getCredential(args.verificationId!!, code)

                FirebaseAuthService.signInWithPhoneAuthCredential(
                    parentActivity,
                    credential,
                    onSuccess = {
                        Log.d(AuthActivity.TAG, "signInWithCredential:success")
                        this.findNavController()
                            .navigate(AuthStepTwoFragmentDirections.actionAuthStepTwoFragmentToAuthStepThreeFragment())
                        toast("Signin successfull: Verification code OK")
                    },
                    onFailure = { exception ->
                        Log.w(AuthActivity.TAG, "signInWithCredential:failure", exception)
                        if (exception is FirebaseAuthInvalidCredentialsException) {
                            binding.editTextVerificationCode.error = "Invalid code."
                            toast("Signin fail: Verification code WRONG")
                        }
                    })
            }
        }

        return binding.root
    }
}