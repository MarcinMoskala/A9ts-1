package com.a9ts.a9ts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.a9ts.a9ts.databinding.AuthBinding
import com.a9ts.a9ts.model.FirebaseAuthService


class AuthActivity : AppCompatActivity() {
    private lateinit var binding: AuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (FirebaseAuthService.auth.currentUser != null) {
            MainActivity.start(this)
        } else {
            binding = AuthBinding.inflate(layoutInflater)
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

    //TODO ugly hack
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


    companion object {
        const val TAG = "AuthActivity"

        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, AuthActivity::class.java))
        }
    }
}




