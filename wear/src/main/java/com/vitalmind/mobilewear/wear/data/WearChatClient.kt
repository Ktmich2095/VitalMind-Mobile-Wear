package com.vitalmind.mobilewear.wear.data

import android.content.Context
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearChatClient(
    private val context: Context
) {

    suspend fun sendQuestion(
        question: String
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
                WearMessagePaths.CHAT_QUESTION,
                question.toByteArray(
                    Charsets.UTF_8
                )
            )
            .await()
    }
}