package com.vitalmind.mobilewear.data.model

import com.google.gson.annotations.SerializedName

data class HabitUpsertRequest(
    val logDate: String,

    val steps: Long? = null,

    @SerializedName("distance_km")
    val distanceKm: Double? = null,

    @SerializedName("calories_kcal")
    val caloriesKcal: Double? = null,

    val sleep: Double? = null
)

data class HabitUpsertResponse(
    val success: Boolean,
    val message: String?
)