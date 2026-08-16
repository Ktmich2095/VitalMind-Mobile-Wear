package com.vitalmind.mobilewear.wear.data

import android.content.Context
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearSensorClient(
    private val context: Context
) {

    suspend fun sendHeartRate(
        heartRate: Int
    ) {

        val nodes =
            Wearable
                .getNodeClient(context)
                .connectedNodes
                .await()

        val node =
            nodes.firstOrNull()
                ?: throw IllegalStateException(
                    "No hay teléfono conectado."
                )

        Wearable
            .getMessageClient(context)
            .sendMessage(
                node.id,
                WearMessagePaths.HEART_RATE,
                heartRate
                    .toString()
                    .toByteArray(
                        Charsets.UTF_8
                    )
            )
            .await()
    }

    suspend fun sendSteps(
        steps: Long
    ) {

        val nodes =
            Wearable
                .getNodeClient(context)
                .connectedNodes
                .await()

        val node =
            nodes.firstOrNull()
                ?: throw IllegalStateException(
                    "No hay teléfono conectado."
                )

        Wearable
            .getMessageClient(context)
            .sendMessage(
                node.id,
                WearMessagePaths.STEPS,
                steps
                    .toString()
                    .toByteArray(
                        Charsets.UTF_8
                    )
            )
            .await()
    }

    suspend fun sendDistanceKm(
        distanceKm: Double
    ) {

        val nodes =
            Wearable
                .getNodeClient(context)
                .connectedNodes
                .await()

        val node =
            nodes.firstOrNull()
                ?: throw IllegalStateException(
                    "No hay teléfono conectado."
                )

        Wearable
            .getMessageClient(context)
            .sendMessage(
                node.id,
                WearMessagePaths.DISTANCE,
                distanceKm
                    .toString()
                    .toByteArray(
                        Charsets.UTF_8
                    )
            )
            .await()
    }

    suspend fun sendCalories(
        caloriesKcal: Double
    ) {

        val nodes =
            Wearable
                .getNodeClient(context)
                .connectedNodes
                .await()

        val node =
            nodes.firstOrNull()
                ?: throw IllegalStateException(
                    "No hay teléfono conectado."
                )

        Wearable
            .getMessageClient(context)
            .sendMessage(
                node.id,
                WearMessagePaths.CALORIES,
                caloriesKcal
                    .toString()
                    .toByteArray(
                        Charsets.UTF_8
                    )
            )
            .await()
    }
}