package com.a9ts.a9ts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.content.ContextCompat
import com.a9ts.a9ts.databinding.ActivityPhoneAuthStepTwoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.jetbrains.anko.toast

class PhoneAuthStepTwo : AppCompatActivity() {
    private lateinit var binding : ActivityPhoneAuthStepTwoBinding
    private lateinit var auth: FirebaseAuth

    private var storedVerificationId: String? = ""
    private lateinit var mainActivityIntent: Intent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthStepTwoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        mainActivityIntent = Intent(this, MainActivity::class.java)

        storedVerificationId = intent.getStringExtra(PhoneAuthStepOne.INTENT_VERIFICATION_ID)

        binding.buttonSendCode.setOnClickListener {
            val code = binding.editTextVerificationCode.text.toString()

            if (TextUtils.isEmpty(code)) {
                binding.editTextVerificationCode.error = "Cannot be empty."
            } else {
                verifyPhoneNumberWithCode(storedVerificationId, code)
            }
        }



    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(PhoneAuthStepOne.TAG, "signInWithCredential:success")

                    startActivity(mainActivityIntent)
                    toast("Signin successfull: Verification code OK")
                } else {
                    Log.w(PhoneAuthStepOne.TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        binding.editTextVerificationCode.error = "Invalid code."
                        toast("Signin fail: Verification code WRONG")
                    }
                }
            }
    }
}