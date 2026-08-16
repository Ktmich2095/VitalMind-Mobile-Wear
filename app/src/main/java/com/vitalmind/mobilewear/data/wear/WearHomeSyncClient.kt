package com.vitalmind.mobilewear.data.wear

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearHomeSyncClient(
    context: Context
) {

    private val dataClient =
        Wearable.getDataClient(
            context.applicationContext
        )

    suspend fun syncHome(
        wellbeingScore: Double?,
        wellbeingLevel: String?,
        riskLevel: String?
    ) {

        if (
            wellbeingScore == null ||
            wellbeingLevel == null ||
            riskLevel == null
        ) {
            return
        }

        val dataMapRequest =
            PutDataMapRequest.create(
                WearDataPaths.HOME_DATA
            )

        dataMapRequest.dataMap.apply {

            putDouble(
                "wellbeing_score",
                wellbeingScore
            )

            putString(
                "wellbeing_level",
                wellbeingLevel
            )

            putString(
                "risk_level",
                riskLevel
            )

            // Hace que exista un cambio
            // incluso si los valores son iguales.
            putLong(
                "updated_at",
                System.currentTimeMillis()
            )
        }

        val request =
            dataMapRequest
                .asPutDataRequest()
                .setUrgent()

        dataClient
            .putDataItem(request)
            .await()
    }
}