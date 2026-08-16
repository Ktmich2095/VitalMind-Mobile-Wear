package com.vitalmind.mobilewear.data.repository

import com.vitalmind.mobilewear.data.api.HabitApiService
import com.vitalmind.mobilewear.data.model.HabitUpsertRequest
import java.time.LocalDate

class HabitRepository(
    private val api: HabitApiService
) {

    suspend fun saveSteps(
        steps: Long
    ) {
        api.upsertHabit(
            HabitUpsertRequest(
                logDate =
                    LocalDate.now()
                        .toString(),
                steps = steps
            )
        )
    }

    suspend fun saveSleep(
        sleepHours: Double
    ) {
        api.upsertHabit(
            HabitUpsertRequest(
                logDate =
                    LocalDate.now()
                        .toString(),
                sleep = sleepHours
            )
        )
    }
    suspend fun saveDistance(
        distanceKm: Double
    ) {
        api.upsertHabit(
            HabitUpsertRequest(
                logDate =
                    LocalDate.now()
                        .toString(),
                distanceKm =
                    distanceKm
            )
        )
    }
    suspend fun saveCalories(
        caloriesKcal: Double
    ) {
        api.upsertHabit(
            HabitUpsertRequest(
                logDate =
                    LocalDate.now()
                        .toString(),
                caloriesKcal =
                    caloriesKcal
            )
        )
    }

}