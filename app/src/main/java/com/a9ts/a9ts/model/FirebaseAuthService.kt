package com.a9ts.a9ts.model

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential

interface AuthService {
    val authUserId: String
    fun getUser(): UserProfile
    fun signInWithPhoneAuthCredential(
        activity: Activity,
        credential: PhoneAuthCredential,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit)

    fun signOut()
    fun getAuth() : FirebaseAuth
    fun getPhoneNumber() : String
}

class FirebaseAuthService : AuthService {
    private val auth = FirebaseAuth.getInstance()
    override val authUserId: String
        get() = auth.uid.toString()

    override fun signInWithPhoneAuthCredential(
        activity: Activity,
        credential: PhoneAuthCredential,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception)
                }
            }
    }

    override fun signOut() {
        auth.signOut()
        // TODO: read somewhere this is needed too, have to read more about it
        // Auth.GoogleSignInApi.signOut(apiClient);
    }

    override fun getAuth(): FirebaseAuth {
        return auth
    }

    override fun getPhoneNumber(): String {
        return auth.currentUser?.phoneNumber.toString()
    }

    override fun getUser(): UserProfile {
        TODO("Not yet implemented")
    }
}