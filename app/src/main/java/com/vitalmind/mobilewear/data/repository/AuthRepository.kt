package com.vitalmind.mobilewear.data.repository

import com.vitalmind.mobilewear.data.api.AuthApiService
import com.vitalmind.mobilewear.data.model.LoginRequest
import com.vitalmind.mobilewear.data.model.LoginResponse

class AuthRepository(
    private val api: AuthApiService
) {

    suspend fun login(
        email: String,
        password: String
    ): LoginResponse {

        return api.login(
            LoginRequest(
                email = email,
                password = password
            )
        )
    }
}