package com.a9ts.a9ts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.a9ts.a9ts.databinding.AuthenticationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import org.jetbrains.anko.toast


class Authentication : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: AuthenticationBinding
    private lateinit var mainActivityIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        //testing
        auth.signOut()

        mainActivityIntent = Intent(this, MainActivity::class.java)

        if (auth.currentUser != null) {
            startActivity(mainActivityIntent)
        } else {
            binding = AuthenticationBinding.inflate(layoutInflater)
            setContentView(binding.root)

            if (savedInstanceState != null) {
                onRestoreInstanceState(savedInstanceState)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (supportActionBar?.title != "Your Phone") {
            showStopVerificationProcessDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun showStopVerificationProcessDialog() {
        AlertDialog.Builder(this)
            .setTitle("A9ts")
            .setMessage("Do you want to stop the verification process?")
            .setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Stop") { dialog, _ ->
                dialog.dismiss()
                super.onBackPressed()
            }
            .create()
            .show()
    }

    internal fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(Authentication.TAG, "signInWithCredential:success")
                    startActivity(mainActivityIntent)
                    toast("Signin successfull: Verification code OK")
                } else {
                    Log.w(Authentication.TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        toast("Signin fail: Verification code WRONG")
                    }
                }
            }
    }

    internal fun getAuth(): FirebaseAuth {
        return auth
    }

    companion object {
        const val TAG = "FirebasePhoneAuth"
        const val INTENT_VERIFICATION_ID = "FirebaseAuthVerificationId"
        const val INTENT_FULL_PHONE_NUMBER = "FirebaseFullPhoneNumber"
    }
}




