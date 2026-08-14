package com.vitalmind.mobilewear.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserDto(
    val id: Long,
    val full_name: String?,
    val email: String,
    val age: Int?,
    val role: String?,
    val status: String?,
    val weight_kg: Double?,
    val height_cm: Double?
)

data class LoginData(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData?
)