package com.vitalmind.mobilewear.wear.health

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StepsManager(
    context: Context
) {

    private val passiveMonitoringClient =
        HealthServices
            .getClient(context)
            .passiveMonitoringClient

    /*
     * Pasos
     */
    private val _steps =
        MutableStateFlow<Long?>(null)

    val steps: StateFlow<Long?> =
        _steps.asStateFlow()

    /*
     * Distancia
     */
    private val _distanceMeters =
        MutableStateFlow<Double?>(null)

    val distanceMeters: StateFlow<Double?> =
        _distanceMeters.asStateFlow()

    /*
     * Calorías
     */
    private val _calories =
        MutableStateFlow<Double?>(null)

    val calories: StateFlow<Double?> =
        _calories.asStateFlow()

    private val callback =
        object : PassiveListenerCallback {

            override fun onNewDataPointsReceived(
                dataPoints: DataPointContainer
            ) {

                /*
                 * Pasos diarios
                 */
                val stepPoints =
                    dataPoints.getData(
                        DataType.STEPS_DAILY
                    )

                val latestSteps =
                    stepPoints
                        .lastOrNull()
                        ?.value

                if (latestSteps != null) {
                    _steps.value =
                        latestSteps
                }

                /*
                 * Distancia diaria
                 */
                val distancePoints =
                    dataPoints.getData(
                        DataType.DISTANCE_DAILY
                    )

                val latestDistance =
                    distancePoints
                        .lastOrNull()
                        ?.value

                if (latestDistance != null) {
                    _distanceMeters.value =
                        latestDistance
                }

                /*
                 * Calorías diarias
                 */
                val caloriesPoints =
                    dataPoints.getData(
                        DataType.CALORIES_DAILY
                    )

                val latestCalories =
                    caloriesPoints
                        .lastOrNull()
                        ?.value

                if (latestCalories != null) {
                    _calories.value =
                        latestCalories
                }
            }
        }

    suspend fun start() {

        val config =
            PassiveListenerConfig
                .builder()
                .setDataTypes(
                    setOf(
                        DataType.STEPS_DAILY,
                        DataType.DISTANCE_DAILY,
                        DataType.CALORIES_DAILY
                    )
                )
                .build()

        passiveMonitoringClient
            .setPassiveListenerCallback(
                config,
                callback
            )
    }

    suspend fun stop() {

        passiveMonitoringClient
            .clearPassiveListenerCallbackAsync()
    }
}