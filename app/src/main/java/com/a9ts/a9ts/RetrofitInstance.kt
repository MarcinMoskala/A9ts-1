package com.a9ts.a9ts

import com.a9ts.a9ts.Constants.Companion.BASE_URL
import com.a9ts.a9ts.model.SystemNotificationAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val api: SystemNotificationAPI by lazy {
            retrofit.create(SystemNotificationAPI::class.java)
        }
    }
}