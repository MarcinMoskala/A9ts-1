package com.a9ts.a9ts
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.a9ts.a9ts.databinding.ActivityPhoneAuthStepOneBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit

class PhoneAuthStepOne : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivityPhoneAuthStepOneBinding

//    private var verificationInProgress = false
    private var storedVerificationId: String = ""
    private var storedFullPhoneNumber: String = ""

//    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken //nerozumiem

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var mainActivityIntent: Intent
    private lateinit var phoneAuthStepTwoIntent : Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivityIntent = Intent(this, MainActivity::class.java)
        phoneAuthStepTwoIntent = Intent(this, PhoneAuthStepTwo::class.java)

        binding = ActivityPhoneAuthStepOneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editTextPhoneNumber.requestFocus()

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }

        auth = Firebase.auth

        supportActionBar?.setTitle(R.string.your_phone)

        binding.buttonGetSmsCode.setOnClickListener {
                startPhoneNumberVerification()
        }



        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            //ked sa overi cislo samo, bez potreby zadavat sms kod
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")

                //TODO zistit naco je verificationInProgress
//                verificationInProgress = false
                signInWithPhoneAuthCredential(credential)
            }

            //ked nevie poslat SMS kod, lebo je napr. zle telefonne cislo
            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    binding.editTextPhoneNumber.error = getString(R.string.invalid_phone_number)
                } else if (e is FirebaseTooManyRequestsException) {
                    toast(getString(R.string.quota_exceeded))
                }
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent:$verificationId")
                toast("SMS Code sent: $verificationId")

                storedVerificationId = verificationId
//              resendToken = token
//              Asi resendToken vobec nepotrebujem

                // TODO prejdi na Step 2 a posli mu vsetky potrebne premenne t.j. storedVerificationId a resendToken (mozno)
                phoneAuthStepTwoIntent.putExtra(INTENT_VERIFICATION_ID, storedVerificationId)
                phoneAuthStepTwoIntent.putExtra(INTENT_FULL_PHONE_NUMBER, storedFullPhoneNumber)

                startActivity(phoneAuthStepTwoIntent)
            }
        }
    }


//    override fun onStart() {
//        super.onStart()
//
//        val currentUser = auth.currentUser
//
//        if (currentUser != null) {
//            startActivity(mainActivityIntent)
//            return
//        }
//
//        if (verificationInProgress) {
//            startPhoneNumberVerification()
//        }
//    }



//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress)
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
//    }

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

                //TODO remove toast
                toast("Submitting $fullPhoneNumber")


                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(fullPhoneNumber)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)                 // Activity (for callback binding)
                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)

                storedFullPhoneNumber = fullPhoneNumber
    //            verificationInProgress = true
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
                        toast("Signin fail: Verification code WRONG")
                    }
                }
            }
    }


    companion object {
        const val TAG = "FirebasePhoneAuth"
        const val INTENT_VERIFICATION_ID = "FirebaseAuthVerificationId"
        const val INTENT_FULL_PHONE_NUMBER = "FirebaseFullPhoneNumber"
//        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
    }
}




