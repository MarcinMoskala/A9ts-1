package com.a9ts.a9ts

import android.content.Context
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
import com.a9ts.a9ts.model.SystemNotificationData
import com.a9ts.a9ts.model.SystemPushNotification
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.android.ext.android.inject
import timber.log.Timber

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
        MyFirebaseMessagingService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        GlobalScope.launch { // askmarcin not sure if this is the right way... GlobalScope is ok?
            MyFirebaseMessagingService.token = FirebaseMessaging.getInstance().token.await()
            // TODO write token to server
            Timber.d("FirebaseMessaging.getInstance: FCM token written to sharedPrefs: ${MyFirebaseMessagingService.token}")
        }

        //FirebaseMessaging.getInstance().subscribeToTopic(SYSTEM_NOTIFICATIONS_TOPIC)

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


    private fun sendSystemNotification(systemNotification: SystemPushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(systemNotification)
            if (response.isSuccessful) {
                //Timber.d("Response: ${Gson().toJson(response)}") // this line was crashing with a stack overflow ... dont know why
            } else {
                Timber.e(response.errorBody().toString())
            }
        } catch (e: Exception) {
            Timber.e(e.toString())
        }
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

            R.id.action_send_notification -> {
//                SystemPushNotification(
//                    SystemNotificationData("Bobs Title", "Bobs message"), SYSTEM_NOTIFICATIONS_TOPIC
//                ).also {
//                    sendSystemNotification(it)
//                }

                SystemPushNotification(
                    SystemNotificationData("Bobs Title", "Bobs message"), MyFirebaseMessagingService.token.toString()
                ).also {
                    sendSystemNotification(it)
                }
                return true
            }

            else -> super.onOptionsItemSelected(item)

        }

    }

    companion object {
        val nextAuthFragmentLabels = listOf("authStepTwo", "authStepThree")
    }
}




