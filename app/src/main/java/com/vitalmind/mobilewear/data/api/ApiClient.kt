package com.vitalmind.mobilewear.data.api

import android.content.Context
import com.vitalmind.mobilewear.data.session.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.vitalmind.mobilewear.data.api.ChatApiService

object ApiClient {

    private const val BASE_URL =
        "http://10.0.2.2:4000/api/"

    private var retrofit: Retrofit? = null

    fun initialize(
        context: Context
    ) {

        if (retrofit != null) {
            return
        }

        val sessionManager =
            SessionManager(
                context.applicationContext
            )

        val okHttpClient =
            OkHttpClient.Builder()
                .addInterceptor(
                    AuthInterceptor(
                        sessionManager
                    )
                )
                .build()

        retrofit =
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .build()
    }

    val authApi: AuthApiService
        get() =
            requireNotNull(retrofit) {
                "ApiClient no ha sido inicializado."
            }.create(
                AuthApiService::class.java
            )

    val mlApi: MlApiService
        get() =
            requireNotNull(retrofit) {
                "ApiClient no ha sido inicializado."
            }.create(
                MlApiService::class.java
            )
    val chatApi: ChatApiService
        get() =
            requireNotNull(retrofit) {
                "ApiClient no ha sido inicializado."
            }.create(
                ChatApiService::class.java
            )
}