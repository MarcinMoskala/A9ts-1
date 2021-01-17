package com.a9ts.a9ts

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.a9ts.a9ts.databinding.AuthBinding
import com.a9ts.a9ts.model.FirebaseAuthService


class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(AuthBinding.inflate(layoutInflater).root)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    //adding Logout to menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuthService.auth.signOut()
                //TODO navigate to first step of AUTH
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}




