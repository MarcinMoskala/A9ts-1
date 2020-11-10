package com.a9ts.a9ts

import android.R
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
    private var storedFullPhoneNumber: String? = ""

    private lateinit var mainActivityIntent: Intent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthStepTwoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        mainActivityIntent = Intent(this, MainActivity::class.java)

        storedVerificationId = intent.getStringExtra(PhoneAuthStepOne.INTENT_VERIFICATION_ID)
        storedFullPhoneNumber = intent.getStringExtra(PhoneAuthStepOne.INTENT_FULL_PHONE_NUMBER)


        supportActionBar?.setTitle(storedFullPhoneNumber)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.buttonSendCode.setOnClickListener {
            val code = binding.editTextVerificationCode.text.toString()

            if (TextUtils.isEmpty(code)) {
                binding.editTextVerificationCode.error = "Cannot be empty."
            } else {
                verifyPhoneNumberWithCode(storedVerificationId, code)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> {
                showStopVerificationProcessDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }


    //aby klasicky back button tiez volal Dialog
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showStopVerificationProcessDialog()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showStopVerificationProcessDialog()
    {
        val myDialog = AlertDialog.Builder(this)

        myDialog.setTitle("A9ts")
        myDialog.setMessage("Do you want to stop the verification process?")

        myDialog.setPositiveButton("Continue") { dialog, _ -> dialog.dismiss()
        }

        myDialog.setNegativeButton("Stop") { dialog, _ ->
                finish()
                dialog.dismiss()
        }

        myDialog.create().show()
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