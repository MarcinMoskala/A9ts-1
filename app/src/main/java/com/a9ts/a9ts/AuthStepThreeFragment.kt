package com.a9ts.a9ts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.a9ts.a9ts.databinding.AuthStepThreeFragmentBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.jetbrains.anko.toast

class AuthStepThreeFragment : Fragment() {
    private lateinit var parentActivity: Authentication
    private lateinit var binding: AuthStepThreeFragmentBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AuthStepThreeFragmentBinding.inflate(inflater, container, false)

        parentActivity = (activity as Authentication)


        parentActivity.supportActionBar?.title = "Profile"
        parentActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        database = Firebase.database

        binding.editTextYourName.requestFocus();

        binding.buttonDone.setOnClickListener {
            val fullName = binding.editTextYourName.text.toString().trim()
            if (fullName.isEmpty()) {
                binding.editTextYourName.error = "Name is required"
                binding.editTextYourName.requestFocus()
            } else {
                saveToFirestore(fullName)
                //updateUserEmail(fullName)
            }
        }

        initFireStore()

        return binding.root
    }

    private fun initFireStore() {
        firestore = FirebaseFirestore.getInstance()
//        channelReference = firestore.collection(CHANNELS).document()
//        query = firestore
//            .collection(CHANNELS)
//            .orderBy(NAME, Query.Direction.ASCENDING)
    }

    //TODO if profile is filled, redirect to Main

    private fun updateUserEmail(email: String) {
        parentActivity.getAuth().currentUser!!.updateEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    parentActivity.toast("Email updated.")
                }
            }
    }

    private fun saveToFirestore(fullName: String){
        val userId = parentActivity.getAuth().currentUser!!.uid
        firestore.collection(USERS).document(userId)
            .set(hashMapOf("name" to fullName))
            .addOnSuccessListener { parentActivity.toast("DocumentSnapshot successfully written!") }
            .addOnFailureListener { parentActivity.toast("Error writing document") }
    }

//    private fun dbSaveFullName(fullName: String) {
//        if (parentActivity.getAuth().currentUser == null) return;
//
//        val uid = parentActivity.getAuth().uid.toString()
//        val myRef = database.getReference("users/$uid")
//        myRef.setValue(fullName)
//        parentActivity.toast("Data sent [$uid:$fullName]")
//    }

    companion object {
        private const val USERS = "users"
    }
}