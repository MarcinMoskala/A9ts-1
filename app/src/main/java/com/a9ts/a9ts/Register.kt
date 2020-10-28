package com.a9ts.a9ts

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.a9ts.a9ts.databinding.RegisterBinding
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = RegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)

        auth = Firebase.auth
        supportActionBar!!.title = "Your Phone Number"

          binding.buttonNext.setOnClickListener {
              val phoneNumber = binding.editTextPhoneNumber.text.toString().trim()
              if (phoneNumber.isEmpty())
              {
                  binding.editTextPhoneNumber.setError("Number is required!")
                  binding.editTextPhoneNumber.requestFocus()
              } else {
                  startPhoneNumberVerification(phoneNumber)
              }
          }

        binding.buttonSendCode.setOnClickListener {
            toast("OK")
        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                Log.d(TAG, "onVerificationCompleted:$credential")
                verificationInProgress = false

                toast("Success!")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                toast("onVerificationFailed")

//                Log.w(TAG, "onVerificationFailed", e)
                verificationInProgress = false

                if (e is FirebaseAuthInvalidCredentialsException) {
//                    Snackbar.make(
//                        findViewById(android.R.id.content), "Ivalid phone number.",
//                        Snackbar.LENGTH_SHORT
//                    ).show()
                    toast("Invalid phone number")
                } else if (e is FirebaseTooManyRequestsException) {
                    toast("SMS Quota exceeded")
                }

                toast("Verify failed")
            }

            override fun onCodeSent(verificationId: String,token: PhoneAuthProvider.ForceResendingToken) {
//                Log.d(TAG, "onCodeSent:$verificationId")

                storedVerificationId = verificationId
                resendToken = token

//                toast("SMS Code sent: $verificationId")
            }
        }
    } // onCreate


    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    toast("User Logged in successfully!")
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        toast("Invalid code entered!")
                    }
                    toast("Signin failed")
                }
            }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                60, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this, // Activity (for callback binding)
                callbacks) // OnVerificationStateChangedCallbacks

        verificationInProgress = true
    }




    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
//            toast("User is logged in.")
        } else {
//            toast("User is NOT logged in.")
        }
    }

    companion object {
        private const val TAG = "PhoneAuthActivity"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_CODE_SENT = 2
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6
    }
}




