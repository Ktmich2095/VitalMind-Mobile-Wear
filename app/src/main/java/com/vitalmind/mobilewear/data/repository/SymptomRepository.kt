package com.vitalmind.mobilewear.data.repository

import com.vitalmind.mobilewear.data.api.SymptomApiService
import com.vitalmind.mobilewear.data.model.HeartRateRequest

class SymptomRepository(
    private val api: SymptomApiService
) {

    suspend fun saveHeartRate(
        heartRate: Int
    ) {

        val symptoms =
            api.getSymptoms(
                page = 1,
                pageSize = 1
            )

        val latest =
            symptoms.items.firstOrNull()

        val request =
            HeartRateRequest(
                heartRate = heartRate,
                notes =
                    "Frecuencia cardiaca sincronizada desde Wear OS"
            )

        if (latest != null) {

            api.updateSymptom(
                id = latest.id,
                request = request
            )

        } else {

            api.createSymptom(
                request = request
            )
        }
    }
}