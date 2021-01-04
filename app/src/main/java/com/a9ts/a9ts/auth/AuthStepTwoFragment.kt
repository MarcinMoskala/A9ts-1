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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import org.jetbrains.anko.toast


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
                    Log.d(AuthActivity.TAG, "signInWithCredential:success")

                    this.findNavController()
                        .navigate(AuthStepTwoFragmentDirections.actionAuthStepTwoFragmentToAuthStepThreeFragment())

                    parentActivity.toast("Signin successfull: Verification code OK")
                } else {
                    Log.w(AuthActivity.TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        binding.editTextVerificationCode.error = "Invalid code."
                        parentActivity.toast("Signin fail: Verification code WRONG")
                    }
                }
            }
    }
}