package com.a9ts.a9ts

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.a9ts.a9ts.databinding.MainBinding
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var authIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authIntent = Intent(this, AuthActivity::class.java)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(authIntent)
        } else {
            val phoneNumber = auth.currentUser?.phoneNumber.toString()
            toast("User: $phoneNumber") // just for debug
        }

        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                firebaseLogout()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun firebaseLogout() {
        auth.signOut()
        startActivity(authIntent)
    }

}