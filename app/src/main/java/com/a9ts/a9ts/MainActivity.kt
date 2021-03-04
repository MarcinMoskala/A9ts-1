package com.a9ts.a9ts

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.a9ts.a9ts.databinding.AcitvityMainBinding
import com.a9ts.a9ts.model.AuthService
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val authService: AuthService by inject()

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = AcitvityMainBinding.inflate(layoutInflater)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // Toolbar setup
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar = binding.myToolbar
        toolbar.setupWithNavController(navController, appBarConfiguration)
        setSupportActionBar(toolbar)

        setContentView(binding.root)

    }


    override fun onBackPressed() {
        val currentFragmentLabel = navController.currentDestination?.label

        if (nextAuthFragmentLabels.contains(currentFragmentLabel)) {
            stopVerificationProcessDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun stopVerificationProcessDialog() {

        AlertDialog.Builder(this)
            .setTitle("A9ts")
            .setMessage("Do you want to stop the verification process?")
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Yes") { dialog, _ ->
                dialog.dismiss()
                authService.signOut()
                this.invalidateOptionsMenu()
                navController.popBackStack(R.id.authStepOneFragment, false)
            }

            .create()
            .show()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                authService.signOut()
                this.invalidateOptionsMenu()

                navController.apply {
                    popBackStack(R.id.mainFragment, true);
                    navigate(R.id.authStepOneFragment)
                }
                return true
            }
            android.R.id.home -> { // if "Up" button pressed, do Back
                onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)

        }

    }

    companion object {
        val nextAuthFragmentLabels = listOf("authStepTwo", "authStepThree")
    }
}




