package com.vitalmind.mobilewear.data.wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.vitalmind.mobilewear.data.api.ApiClient
import com.vitalmind.mobilewear.data.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

class WearMessageService :
    WearableListenerService() {

    private val serviceScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        )

    override fun onMessageReceived(
        messageEvent: MessageEvent
    ) {
        if (
            messageEvent.path !=
            WearMessagePaths.CHAT_QUESTION
        ) {
            return
        }

        val question =
            messageEvent.data.toString(
                Charsets.UTF_8
            )

        Log.d(
            "VitalMindWear",
            "Pregunta recibida: $question"
        )

        serviceScope.launch {

            try {

                val repository =
                    ChatRepository(
                        ApiClient.chatApi
                    )

                val response =
                    repository.ask(
                        analysisDate =
                            LocalDate.now()
                                .toString(),
                        message = question
                    )

                val answer =
                    response.data?.answer
                        ?: "No fue posible generar una respuesta."

                Wearable
                    .getMessageClient(
                        applicationContext
                    )
                    .sendMessage(
                        messageEvent.sourceNodeId,
                        WearMessagePaths.CHAT_ANSWER,
                        answer.toByteArray(
                            Charsets.UTF_8
                        )
                    )

            } catch (error: Exception) {

                Log.e(
                    "VitalMindWear",
                    "Error procesando pregunta",
                    error
                )

                val answer =
                    "No fue posible obtener una respuesta de VitalMind."

                Wearable
                    .getMessageClient(
                        applicationContext
                    )
                    .sendMessage(
                        messageEvent.sourceNodeId,
                        WearMessagePaths.CHAT_ANSWER,
                        answer.toByteArray(
                            Charsets.UTF_8
                        )
                    )
            }
        }
    }
}