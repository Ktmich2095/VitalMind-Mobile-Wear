package com.vitalmind.mobilewear.data.wear

import android.util.Log
import androidx.compose.foundation.layout.Box
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.vitalmind.mobilewear.data.api.ApiClient
import com.vitalmind.mobilewear.data.repository.ChatRepository
import com.vitalmind.mobilewear.data.repository.SymptomRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.vitalmind.mobilewear.data.repository.HabitRepository

class WearMessageService :
    WearableListenerService() {

    private val serviceScope =
        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        )

    override fun onCreate() {
        super.onCreate()

        ApiClient.initialize(
            applicationContext
        )
    }

    override fun onMessageReceived(
        messageEvent: MessageEvent
    ) {
        when (messageEvent.path) {

            WearMessagePaths.CHAT_QUESTION -> {
                handleChatQuestion(
                    messageEvent
                )
            }

            WearMessagePaths.HEART_RATE -> {
                handleHeartRate(
                    messageEvent
                )
            }

            WearMessagePaths.STEPS -> {
                handleSteps(
                    messageEvent
                )
            }

            WearMessagePaths.DISTANCE -> {
                handleDistance(
                    messageEvent
                )
            }

            WearMessagePaths.CALORIES -> {
                handleCalories(
                    messageEvent
                )
            }

            else -> {
                Log.d(
                    "VitalMindWear",
                    "Mensaje ignorado: ${messageEvent.path}"
                )
            }
        }
    }
    private fun handleDistance(
        messageEvent: MessageEvent
    ) {
        val distanceKm =
            messageEvent.data
                .toString(
                    Charsets.UTF_8
                )
                .toDoubleOrNull()

        if (distanceKm == null) {
            Log.e(
                "VitalMindWear",
                "Distancia recibida inválida."
            )
            return
        }

        Log.d(
            "VitalMindWear",
            "Distancia recibida del reloj: $distanceKm km"
        )

        serviceScope.launch {

            try {

                val repository =
                    HabitRepository(
                        ApiClient.habitApi
                    )

                repository.saveDistance(
                    distanceKm
                )

                Log.d(
                    "VitalMindWear",
                    "Distancia guardada en Backend: $distanceKm km"
                )

            } catch (error: Exception) {

                Log.e(
                    "VitalMindWear",
                    "Error guardando distancia",
                    error
                )
            }
        }
    }
    private fun handleSteps(
        messageEvent: MessageEvent
    ) {
        val steps =
            messageEvent.data
                .toString(
                    Charsets.UTF_8
                )
                .toLongOrNull()

        if (steps == null) {
            Log.e(
                "VitalMindWear",
                "Cantidad de pasos inválida."
            )
            return
        }

        Log.d(
            "VitalMindWear",
            "Pasos recibidos del reloj: $steps"
        )

        serviceScope.launch {

            try {

                val repository =
                    HabitRepository(
                        ApiClient.habitApi
                    )

                repository.saveSteps(
                    steps
                )

                Log.d(
                    "VitalMindWear",
                    "Pasos guardados en Backend: $steps"
                )

            } catch (error: Exception) {

                Log.e(
                    "VitalMindWear",
                    "Error guardando pasos",
                    error
                )
            }
        }
    }

    private fun handleChatQuestion(
        messageEvent: MessageEvent
    ) {
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

                Log.d(
                    "VitalMindWear",
                    "Respuesta enviada al reloj."
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
    private fun handleCalories(
        messageEvent: MessageEvent
    ) {
        val caloriesKcal =
            messageEvent.data
                .toString(
                    Charsets.UTF_8
                )
                .toDoubleOrNull()

        if (caloriesKcal == null) {
            Log.e(
                "VitalMindWear",
                "Calorías recibidas inválidas."
            )
            return
        }

        Log.d(
            "VitalMindWear",
            "Calorías recibidas del reloj: $caloriesKcal kcal"
        )

        serviceScope.launch {

            try {

                val repository =
                    HabitRepository(
                        ApiClient.habitApi
                    )

                repository.saveCalories(
                    caloriesKcal
                )

                Log.d(
                    "VitalMindWear",
                    "Calorías guardadas en Backend: $caloriesKcal kcal"
                )

            } catch (error: Exception) {

                Log.e(
                    "VitalMindWear",
                    "Error guardando calorías",
                    error
                )
            }
        }
    }

    private fun handleHeartRate(
        messageEvent: MessageEvent
    ) {
        val heartRate =
            messageEvent.data
                .toString(
                    Charsets.UTF_8
                )
                .toIntOrNull()

        if (heartRate == null) {
            Log.e(
                "VitalMindWear",
                "Frecuencia cardiaca inválida."
            )
            return
        }

        Log.d(
            "VitalMindWear",
            "Frecuencia recibida del reloj: $heartRate bpm"
        )

        serviceScope.launch {

            try {
                val repository =
                    SymptomRepository(
                        ApiClient.symptomApi
                    )

                repository.saveHeartRate(
                    heartRate
                )

                Log.d(
                    "VitalMindWear",
                    "Frecuencia guardada en Backend: $heartRate bpm"
                )

            } catch (error: Exception) {

                Log.e(
                    "VitalMindWear",
                    "Error guardando frecuencia cardiaca",
                    error
                )
            }
        }
    }
}