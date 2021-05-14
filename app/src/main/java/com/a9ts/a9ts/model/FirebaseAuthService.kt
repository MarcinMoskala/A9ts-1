package com.a9ts.a9ts.model

import android.app.Activity
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import kotlinx.coroutines.tasks.await

interface AuthService {
    val authUserId: String
    val isLogged: Boolean
    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) : AuthResult

    fun signOut()
    fun getAuth() : FirebaseAuth
    fun getPhoneNumber() : String
}

class FirebaseAuthService : AuthService {
    private val auth = FirebaseAuth.getInstance()

    override val authUserId: String
        get() = auth.uid.toString()

    override val isLogged: Boolean
        get() = auth.uid != null

    override suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) : AuthResult {
        return auth.signInWithCredential(credential).await()

        // TODO - handle the exception: auth.signInWithCredential(credential).exception
        // askmarcin how should I deal with the exception when I want to use .await()?
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

//    override fun getFullUserName(): String {
//        return auth.currentUser?.displayName.toString()
//    }
}