package com.vitalmind.mobilewear.data.api

import com.vitalmind.mobilewear.data.model.LoginRequest
import com.vitalmind.mobilewear.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse
}