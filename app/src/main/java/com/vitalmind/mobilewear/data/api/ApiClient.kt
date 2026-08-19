package com.vitalmind.mobilewear.data.api

import android.content.Context
import com.vitalmind.mobilewear.BuildConfig
import com.vitalmind.mobilewear.data.session.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

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
                .baseUrl(
                    BuildConfig.API_BASE_URL
                )
                .client(
                    okHttpClient
                )
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

    val symptomApi: SymptomApiService
        get() =
            requireNotNull(retrofit) {
                "ApiClient no ha sido inicializado."
            }.create(
                SymptomApiService::class.java
            )

    val habitApi: HabitApiService
        get() =
            requireNotNull(retrofit) {
                "ApiClient no ha sido inicializado."
            }.create(
                HabitApiService::class.java
            )
}