package com.a9ts.a9ts

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.a9ts.a9ts.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var registerIntent: Intent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // -- check if logged in, if not -> redirect to PhoneAuthStepOne
        registerIntent = Intent(this, PhoneAuthStepOne::class.java)

        auth = Firebase.auth

        if (auth.currentUser == null) {
            startActivity(registerIntent)
        } else {
            val phoneNumber = auth.currentUser?.phoneNumber.toString()
            toast("User: $phoneNumber")
        }
        // -- end of check


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }





    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
        startActivity(registerIntent)
    }
}