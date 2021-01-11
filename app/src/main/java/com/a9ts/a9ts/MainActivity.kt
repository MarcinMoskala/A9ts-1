package com.a9ts.a9ts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.a9ts.a9ts.databinding.MainBinding
import com.a9ts.a9ts.model.FirebaseAuthService
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (FirebaseAuthService.auth.currentUser == null) {
            AuthActivity.start(this)
        } else { // just to know if it works
            val phoneNumber = FirebaseAuthService.auth.currentUser?.phoneNumber.toString()
            toast("Welcome user: $phoneNumber")
        }

        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                AuthActivity.start(this)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
        }
    }

}