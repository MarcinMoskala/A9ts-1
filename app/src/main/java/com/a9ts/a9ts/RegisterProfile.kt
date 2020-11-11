package com.a9ts.a9ts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.a9ts.a9ts.databinding.ActivityRegisterProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.jetbrains.anko.toast

class RegisterProfile : AppCompatActivity() {
    private lateinit var binding : ActivityRegisterProfileBinding
    private lateinit var registerIntent : Intent
    private lateinit var auth: FirebaseAuth
    private lateinit var database : FirebaseDatabase

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

        database = Firebase.database

        binding = ActivityRegisterProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setTitle("Profile")
        binding.editTextYourName.requestFocus();

        binding.buttonDone.setOnClickListener() {
            val fullName = binding.editTextYourName.text.toString().trim()
            if (fullName.isEmpty()) {
                binding.editTextYourName.error = "Name is required"
                binding.editTextYourName.requestFocus()
            } else {
//                dbSaveFullName(fullName)
                updateUserEmail(fullName)
            }
        }

        //TODO ak uz ma profil tak rovno na Main ist. Tu by sa mal vyskytnut iba ked prvy krat overuje cislo a nema profil
    }

    private fun updateUserEmail(email: String) {
        auth.currentUser!!.updateEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toast("Email updated.")
                }
            }
    }

    fun dbSaveFullName(fullName : String) {

        if (auth.currentUser == null) return;

        val uid = auth.uid.toString()
        val myRef = database.getReference("users/$uid")
        myRef.setValue(fullName)

        toast("Data sent [$uid:$fullName]")
    }
}