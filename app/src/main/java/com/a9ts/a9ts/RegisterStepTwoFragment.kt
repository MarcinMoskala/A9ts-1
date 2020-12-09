package com.a9ts.a9ts

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.a9ts.a9ts.databinding.RegisterStepTwoFragmentBinding
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import org.jetbrains.anko.toast


class RegisterStepTwoFragment : Fragment() {
    private lateinit var binding: RegisterStepTwoFragmentBinding
    private lateinit var parentActivity: Authentication

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RegisterStepTwoFragmentBinding.inflate(inflater, container, false)
        parentActivity = (activity as Authentication)

        val args = RegisterStepTwoFragmentArgs.fromBundle(requireArguments())

        if (args.fullPhoneNumber.isEmpty() || args.verificationId.isEmpty()) {
            this.findNavController().navigate(RegisterStepTwoFragmentDirections.actionRegisterStepTwoFragmentToRegisterStepOneFragment())
        }

        parentActivity.supportActionBar?.title = args.fullPhoneNumber
        parentActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.editTextVerificationCode.requestFocus()

        binding.buttonSendCode.setOnClickListener {
            val code = binding.editTextVerificationCode.text.toString()

            if (TextUtils.isEmpty(code)) {
                binding.editTextVerificationCode.error = "Cannot be empty."
            } else {
                verifyPhoneNumberWithCode(args.verificationId, code)
            }
        }

        return binding.root
    }


    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        parentActivity.getAuth().signInWithCredential(credential)
            .addOnCompleteListener(parentActivity) { task ->
                if (task.isSuccessful) {
                    Log.d(Authentication.TAG, "signInWithCredential:success")

                    this.findNavController().navigate(RegisterStepTwoFragmentDirections.actionRegisterStepTwoFragmentToRegisterStepThreeFragment())

                    parentActivity.toast("Signin successfull: Verification code OK")
                } else {
                    Log.w(Authentication.TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        binding.editTextVerificationCode.error = "Invalid code."
                        parentActivity.toast("Signin fail: Verification code WRONG")
                    }
                }
            }
    }
}