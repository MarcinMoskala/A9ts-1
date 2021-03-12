package com.a9ts.a9ts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.a9ts.a9ts.Constants.Companion.CHANNEL_ID
import com.a9ts.a9ts.Constants.Companion.CHANNEL_NAME
import com.a9ts.a9ts.databinding.AcitvityMainBinding
import com.a9ts.a9ts.model.AuthService
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.android.ext.android.inject
import timber.log.Timber


class MainActivity : AppCompatActivity() {
    private val authService: AuthService by inject()
    private lateinit var navController: NavController

    private val viewModel: MainActivityViewModel by lazy {
        ViewModelProvider(this).get(MainActivityViewModel::class.java)
    }

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


        createSystemNotificationChannel()
        clearAllSystemNotifications()

        viewModel.onCreateView()

        viewModel.authUserId.observe(this) { authUserId ->
            getFirebaseDeviceToken(authUserId) //not sure if I have to call it here everytime the app starts.. the deviceToken should be in SharedPrefs from registration...
        }
    }

    fun getFirebaseDeviceToken(authUserId: String) {
        if (MyFirebaseMessagingService.getToken(this, authUserId).isNullOrEmpty()) {
            lifecycleScope.launch {
                MyFirebaseMessagingService.saveToken(this@MainActivity, authUserId, token = FirebaseMessaging.getInstance().token.await())
                // TODO write token to server
                Timber.d("FirebaseMessaging.getInstance: FCM token written to sharedPrefs: ${MyFirebaseMessagingService.getToken(this@MainActivity, authUserId)}")
            }
        } else {
            Timber.d("FirebaseDeviceToken taken from SharedPrefs: ${MyFirebaseMessagingService.getToken(this@MainActivity, authUserId)}")
        }
    }


    private fun clearAllSystemNotifications() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun createSystemNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                // TODO set it up properly with appropirate CHANNEL_NAME sensible default etc
                lightColor = Color.GREEN //rozsvieti LED na nasom telefon nejakou farbou
                enableLights(true)
                description = "New A9ts notification"
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
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
                    popBackStack(R.id.mainFragment, true)
                    navigate(R.id.authStepOneFragment)
                }
                return true
            }
            android.R.id.home -> { // if "Up" button pressed, do Back
                onBackPressed()
                return true
            }

            R.id.action_clear -> {
                viewModel.clearSharedPrefs(applicationContext)
                return true
            }
            else -> super.onOptionsItemSelected(item)

        }

    }

    companion object {
        val nextAuthFragmentLabels = listOf("authStepTwo", "authStepThree")
    }
}




