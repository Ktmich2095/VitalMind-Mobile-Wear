package com.vitalmind.mobilewear.wear.health

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.unregisterMeasureCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HeartRateManager(
    context: Context
) {

    private val measureClient =
        HealthServices
            .getClient(context)
            .measureClient

    private val _heartRate =
        MutableStateFlow<Double?>(null)

    val heartRate: StateFlow<Double?> =
        _heartRate.asStateFlow()

    private val heartRateDataType =
        DataType.HEART_RATE_BPM

    private val callback =
        object : MeasureCallback {

            override fun onAvailabilityChanged(
                dataType: DeltaDataType<*, *>,
                availability: Availability
            ) {
                // Por ahora no necesitamos mostrar
                // el estado de disponibilidad.
            }

            override fun onDataReceived(
                data: DataPointContainer
            ) {

                val points =
                    data.getData(
                        heartRateDataType
                    )

                val latest =
                    points.lastOrNull()
                        ?.value

                if (latest != null) {
                    _heartRate.value =
                        latest
                }
            }
        }

    fun start() {
        measureClient.registerMeasureCallback(
            heartRateDataType,
            callback
        )
    }

    suspend fun stop() {
        measureClient.unregisterMeasureCallback(
            heartRateDataType,
            callback
        )
    }
}