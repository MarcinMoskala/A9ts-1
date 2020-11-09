package com.a9ts.a9ts
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.a9ts.a9ts.databinding.RegisterBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit

class Register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding : RegisterBinding

    private var verificationInProgress = false
    private var storedVerificationId: String? = ""



    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken //nerozumiem
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var mainActivityIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityIntent = Intent(this, MainActivity::class.java)
        binding = RegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }

        auth = Firebase.auth

        getSupportActionBar()?.setTitle(R.string.your_phone)

        binding.buttonGetSmsCode.setOnClickListener {
            val phoneNumber = binding.editTextPhoneNumber.text.toString().trim()
            val countryCode = binding.editTextCountryCode.text.toString().trim()

            if (TextUtils.isEmpty(phoneNumber))
            {
                binding.editTextPhoneNumber.setError("Phone number is required!")
                binding.editTextPhoneNumber.requestFocus()
            } else if (TextUtils.isEmpty(countryCode)) {
                binding.editTextCountryCode.setError("Country code is required!")
                binding.editTextCountryCode.requestFocus()
            } else {
                var fullPhoneNumber = countryCode + phoneNumber

                if (!fullPhoneNumber.startsWith("+")) {
                    fullPhoneNumber = "+$fullPhoneNumber"
                }

                toast("Submitting $fullPhoneNumber")
                startPhoneNumberVerification(fullPhoneNumber)
            }
        }

        binding.buttonSendCode.setOnClickListener {
            val code = binding.editTextVerificationCode.text.toString()

            if (TextUtils.isEmpty(code)) {
                binding.editTextVerificationCode.error = "Cannot be empty."
            } else {
                verifyPhoneNumberWithCode(storedVerificationId, code)
            }
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            //ked sa overi cislo samo, bez potreby zadavat sms kod
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                //TODO: debug toast
                toast("onVerificationCompleted")

                Log.d(TAG, "onVerificationCompleted:$credential")
                verificationInProgress = false

                signInWithPhoneAuthCredential(credential)
            }

            //ked nevie poslat SMS kod, lebo je napr. zle telefonne cislo
            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    binding.editTextPhoneNumber.error = getString(R.string.invalid_phone_number)
                } else if (e is FirebaseTooManyRequestsException) {
                    //TODO: Iny text dat
                    toast("Quota exceeded!")
                }
                toast("onVerificationFailed")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
                toast("SMS Code sent: $verificationId")
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")

                    startActivity(mainActivityIntent)
                    toast("Signin successfull: Verification code OK")
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        binding.editTextVerificationCode.error = "Invalid code."
                        toast("Signin fail: Verification code WRONG")
                    }
                }
            }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    companion object {
        private const val TAG = "PhoneAuth"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_CODE_SENT = 2
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6
    }
}




