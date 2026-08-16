package com.vitalmind.mobilewear.wear.data

import android.net.Uri
import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WearHomeDataListener(
    context: Context
) : DataClient.OnDataChangedListener {

    private val dataClient =
        Wearable.getDataClient(
            context.applicationContext
        )

    private val scope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        )

    private val _state =
        MutableStateFlow(
            WearHomeState()
        )

    val state: StateFlow<WearHomeState> =
        _state.asStateFlow()

    fun start() {

        dataClient.addListener(this)

        loadCurrentData()
    }

    fun stop() {
        dataClient.removeListener(this)
    }

    private fun loadCurrentData() {

        scope.launch {

            try {

                val uri =
                    Uri.parse(
                        "wear://*/vitalmind/home"
                    )

                val dataItems =
                    dataClient
                        .getDataItems(
                            uri,
                            DataClient.FILTER_LITERAL
                        )
                        .await()

                try {

                    if (dataItems.count > 0) {

                        val item =
                            dataItems[0]

                        updateFromDataItem(
                            item
                        )
                    }

                } finally {
                    dataItems.release()
                }

            } catch (error: Exception) {
                // Por ahora dejamos el estado
                // vacío si no hay datos disponibles.
            }
        }
    }

    override fun onDataChanged(
        dataEvents: DataEventBuffer
    ) {

        dataEvents.forEach { event ->

            if (
                event.type !=
                DataEvent.TYPE_CHANGED
            ) {
                return@forEach
            }

            val item =
                event.dataItem

            if (
                item.uri.path !=
                "/vitalmind/home"
            ) {
                return@forEach
            }

            updateFromDataItem(
                item
            )
        }
    }

    private fun updateFromDataItem(
        item: com.google.android.gms.wearable.DataItem
    ) {

        val dataMap =
            DataMapItem
                .fromDataItem(item)
                .dataMap

        _state.value =
            WearHomeState(
                wellbeingScore =
                    dataMap.getDouble(
                        "wellbeing_score"
                    ),
                wellbeingLevel =
                    dataMap.getString(
                        "wellbeing_level"
                    ),
                riskLevel =
                    dataMap.getString(
                        "risk_level"
                    )
            )
    }
}