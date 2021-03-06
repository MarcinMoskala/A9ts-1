package com.a9ts.a9ts.model

import com.a9ts.a9ts.Constants.Companion.CONTENT_TYPE
import com.a9ts.a9ts.Constants.Companion.SERVER_KEY
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SystemNotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: SystemPushNotification
    ): Response<ResponseBody>
}