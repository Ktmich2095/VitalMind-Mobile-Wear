package com.vitalmind.mobilewear.data.api

import com.vitalmind.mobilewear.data.model.HabitUpsertRequest
import com.vitalmind.mobilewear.data.model.HabitUpsertResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface HabitApiService {

    @POST("habits")
    suspend fun upsertHabit(
        @Body request: HabitUpsertRequest
    ): HabitUpsertResponse
}