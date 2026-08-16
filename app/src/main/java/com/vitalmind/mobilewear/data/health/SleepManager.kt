package com.vitalmind.mobilewear.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class SleepManager(
    context: Context
) {

    private val healthConnectClient =
        HealthConnectClient.getOrCreate(
            context.applicationContext
        )

    val sleepPermission =
        HealthPermission.getReadPermission(
            SleepSessionRecord::class
        )



    suspend fun hasPermission(): Boolean {

        val grantedPermissions =
            healthConnectClient
                .permissionController
                .getGrantedPermissions()

        return sleepPermission in grantedPermissions

    }

    suspend fun getLatestSleepHours(): Double? {

        val endTime =
            Instant.now()

        val startTime =
            endTime.minus(
                7,
                ChronoUnit.DAYS
            )

        val response =
            healthConnectClient.readRecords(
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