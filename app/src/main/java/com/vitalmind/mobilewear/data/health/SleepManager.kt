package com.vitalmind.mobilewear.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class SleepManager(
    context: Context
) {

    private val appContext =
        context.applicationContext

    val sleepPermission =
        HealthPermission.getReadPermission(
            SleepSessionRecord::class
        )

    fun isAvailable(): Boolean {

        return HealthConnectClient
            .getSdkStatus(
                appContext,
                "com.google.android.apps.healthdata"
            ) == HealthConnectClient.SDK_AVAILABLE
    }

    private fun getClient(): HealthConnectClient {

        check(isAvailable()) {
            "Health Connect no está disponible en este dispositivo."
        }

        return HealthConnectClient
            .getOrCreate(
                appContext
            )
    }
    suspend fun hasPermission(): Boolean {

        if (!isAvailable()) {
            return false
        }

        val grantedPermissions =
            getClient()
                .permissionController
                .getGrantedPermissions()

        return sleepPermission in grantedPermissions
    }

    suspend fun getLatestSleepHours(): Double? {

        if (!isAvailable()) {
            return null
        }

        val endTime =
            Instant.now()

        val startTime =
            endTime.minus(
                7,
                ChronoUnit.DAYS
            )

        val response =
            getClient().readRecords(
                ReadRecordsRequest(
                    recordType =
                        SleepSessionRecord::class,

                    timeRangeFilter =
                        TimeRangeFilter.between(
                            startTime,
                            endTime
                        ),

                    ascendingOrder = false,
                    pageSize = 1
                )
            )

        val latestSession =
            response.records
                .firstOrNull()
                ?: return null

        val duration =
            Duration.between(
                latestSession.startTime,
                latestSession.endTime
            )

        return duration.toMinutes() / 60.0
    }
}