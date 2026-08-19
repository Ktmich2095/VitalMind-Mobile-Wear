package com.vitalmind.mobilewear.data.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearHomeSyncClient(
    context: Context
) {

    private val appContext =
        context.applicationContext

    suspend fun syncHome(
        wellbeingScore: Double?,
        wellbeingLevel: String?,
        riskLevel: String?,
        recommendations: List<String>
    ) {
        if (
            wellbeingScore == null ||
            wellbeingLevel == null ||
            riskLevel == null
        ) {
            return
        }

        try {
            val dataClient =
                Wearable.getDataClient(
                    appContext
                )

            val dataMapRequest =
                PutDataMapRequest.create(
                    "/vitalmind/home"
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

                putStringArrayList(
                    "recommendations",
                    ArrayList(recommendations)
                )

                putLong(
                    "updated_at",
                    System.currentTimeMillis()
                )
            }

            dataClient
                .putDataItem(
                    dataMapRequest
                        .asPutDataRequest()
                        .setUrgent()
                )
                .await()

        } catch (error: Exception) {

            Log.w(
                "VitalMindWear",
                "Wear OS no disponible. Se omite sincronización.",
                error
            )
        }
    }
}