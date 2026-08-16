package com.vitalmind.mobilewear.wear.data

import android.content.Context
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class WearConnectionManager(
    context: Context
) {

    private val nodeClient =
        Wearable.getNodeClient(
            context.applicationContext
        )

    private val _isConnected =
        MutableStateFlow(false)

    val isConnected: StateFlow<Boolean> =
        _isConnected.asStateFlow()

    suspend fun checkConnection() {

        try {

            val nodes =
                nodeClient.connectedNodes.await()

            _isConnected.value =
                nodes.isNotEmpty()

        } catch (error: Exception) {

            _isConnected.value =
                false
        }
    }
}