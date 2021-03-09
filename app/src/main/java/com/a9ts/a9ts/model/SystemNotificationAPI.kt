package com.a9ts.a9ts.model

import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.*

interface SystemNotificationAPI {

    //@Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    //@Headers("Content-Type:application/json")
    @GET("send")
    suspend fun postNotification(
        @Query("title") title: String,
        @Query("body") body: String,
        @Query("token") token: String
    ): Response<ResponseBody>
}