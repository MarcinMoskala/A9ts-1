package com.a9ts.a9ts.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.a9ts.a9ts.AuthActivity
import com.a9ts.a9ts.MainActivity
import com.a9ts.a9ts.databinding.AuthStepThreeFragmentBinding
import com.a9ts.a9ts.model.FirebaseAuthService
import com.a9ts.a9ts.model.FirestoreService
import org.jetbrains.anko.support.v4.toast

class AuthStepThreeFragment : Fragment() {
    private lateinit var parentActivity: AuthActivity
    private lateinit var binding: AuthStepThreeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AuthStepThreeFragmentBinding.inflate(inflater, container, false)

        //not sure why the casting has to be here, but without it I can't do things
        //like activity.supportAtionBar?.title etc...
        //probably the whole thing should be done in a different way? Somewhere in the activity...

        parentActivity = (activity as AuthActivity)

        parentActivity.supportActionBar?.title = "Profile"
        parentActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.editTextYourName.requestFocus();

        binding.buttonDone.setOnClickListener {
            val fullName = binding.editTextYourName.text.toString().trim()

            if (fullName.isEmpty()) {
                binding.editTextYourName.error = "Name is required"
                binding.editTextYourName.requestFocus()
            } else {
                val userId = FirebaseAuthService.auth.uid.toString()

                FirestoreService.saveUser(userId, fullName,
                    success = {
                        toast("DocumentSnapshot successfully written!")
                        MainActivity.start(parentActivity)
                    },
                    failure = {exception ->
                        toast("Error writing document: ${exception.toString()}")
                    })
            }
        }
        return binding.root
    }
}